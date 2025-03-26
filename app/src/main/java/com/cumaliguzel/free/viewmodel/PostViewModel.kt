package com.cumaliguzel.free.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cumaliguzel.free.data.model.Post
import com.cumaliguzel.free.data.repository.PostRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _remainingTimeMap = mutableStateMapOf<String, MutableStateFlow<Long>>() // 🔥 Sayaçları burada saklıyoruz
    val remainingTimeMap: Map<String, StateFlow<Long>> get() = _remainingTimeMap

    private var postListener: ListenerRegistration? = null

    init {
        fetchPosts()
    }

    /**
     * Firestore'daki ilanları (postları) gerçek zamanlı dinler.
     */
    private fun fetchPosts() {
        postListener = postRepository.listenForPosts(
            onPostsUpdated = { postList ->
                _posts.value = postList.map { post ->
                    post.copy(
                        fromLocation = getFormattedAddress(post.fromLocation),
                        toLocation = getFormattedAddress(post.toLocation),
                        distanceKm = post.distanceKm,  // ✅ Mesafe bilgisi alındı
                        estimatedDuration = post.estimatedDuration  // ✅ Süre bilgisi alındı
                    )
                }
                postList.forEach { post ->
                    if (!_remainingTimeMap.containsKey(post.postId)) {
                        startCountdown(post)
                    }
                }
            },
            onFailure = { error ->
                println("Hata: ${error.message}")
            }
        )
    }

    /**
     * **Adresleri daha düzgün hale getiren fonksiyon**
     */
    private fun getFormattedAddress(address: String): String {
        return if (address.contains("Bilinmeyen", ignoreCase = true) || address.isBlank()) {
            "Adres bilgisi eksik"
        } else {
            address
        }
    }

    /**
     * Yeni bir ilan ekler.
     */
    fun addPost(post: Post) {
        viewModelScope.launch {
            val formattedPost = post.copy(
                fromLocation = getFormattedAddress(post.fromLocation),
                toLocation = getFormattedAddress(post.toLocation)
            )
            postRepository.addPost(
                post = formattedPost,
                onSuccess = { println("Post başarıyla eklendi!") },
                onFailure = { error -> println("Hata: ${error.message}") }
            )
        }
    }

    /**
     * Geri sayımı başlat ve sayaç değerini sakla
     */
    private fun startCountdown(post: Post) {
        val remainingTime = getRemainingTime(post.deleteAt)
        _remainingTimeMap[post.postId] = MutableStateFlow(remainingTime)

        viewModelScope.launch {
            while ((_remainingTimeMap[post.postId]?.value ?: 0) > 0) {
                delay(1000L)
                _remainingTimeMap[post.postId]?.value = (_remainingTimeMap[post.postId]?.value ?: 0) - 1
            }
            deletePost(post.postId) // 📌 Sayaç bittiğinde postu sil
        }
    }

    /**
     * Belirli bir post'u Firestore'dan siler.
     */
    private fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(
                postId,
                onSuccess = {
                    println("Post başarıyla silindi: $postId")
                    _remainingTimeMap.remove(postId) // 🔥 Sayaç bilgisini de temizle
                },
                onFailure = { error -> println("Post silme hatası: ${error.message}") }
            )
        }
    }

    /**
     * Verilen postun silinmesine kalan süreyi hesaplar.
     */
    private fun getRemainingTime(deleteAt: Timestamp): Long {
        val currentTimeMillis = System.currentTimeMillis()
        val deleteAtMillis = deleteAt.toDate().time
        val remainingMillis = deleteAtMillis - currentTimeMillis
        return (remainingMillis / 1000).coerceAtLeast(0)
    }

    /**
     * Geri sayım süresini `MM:SS` formatına çevirir.
     */
    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }

    /**
     * Tarih formatını düzenler.
     */
    fun formatDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    /**
     * Fiyatı tam sayı olarak formatlar.
     */
    fun formatEstimatedPrice(priceString: String): String {
        return priceString.replace(Regex("\\d+\\.\\d+")) { matchResult ->
            matchResult.value.toDouble().toInt().toString() // Ondalığı at, tam sayı göster
        }
    }

    override fun onCleared() {
        super.onCleared()
        postListener?.remove()
    }
}