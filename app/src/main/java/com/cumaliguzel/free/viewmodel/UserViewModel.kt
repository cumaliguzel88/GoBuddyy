package com.cumaliguzel.free.viewmodel

import androidx.lifecycle.ViewModel
import com.cumaliguzel.free.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    /**
     * 🔥 Kullanıcının adını ve profil fotoğrafını Firestore’dan alır.
     */
    fun fetchUserDetails(userId: String, onUserFetched: (User) -> Unit) {
        if (userId.isEmpty()) {
            println("❌ fetchUserDetails: Boş userId geldi!")
            return
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = User(
                        userId = document.getString("userId") ?: "",
                        name = document.getString("name") ?: "Bilinmeyen Kullanıcı",
                        email = document.getString("email") ?: "",
                        photoUrl = document.getString("photoUrl") ?: "",
                        createdAt = document.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now()
                    )

                    println("✅ Firestore'dan gelen kullanıcı: $user")
                    onUserFetched(user) // Kullanıcı bilgilerini PostCard'a ilet
                } else {
                    println("❌ Kullanıcı bulunamadı: $userId")
                }
            }
            .addOnFailureListener { error ->
                println("❌ Firestore Hata: ${error.message}")
            }
    }
}