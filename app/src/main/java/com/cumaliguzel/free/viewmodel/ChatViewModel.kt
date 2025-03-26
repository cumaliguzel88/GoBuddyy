package com.cumaliguzel.free.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cumaliguzel.free.data.model.ChatMessage

import com.cumaliguzel.free.domain.usecase.ListenMessagesUseCase
import com.cumaliguzel.free.domain.usecase.SendMessageUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val listenMessagesUseCase: ListenMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _chatUser = MutableStateFlow<ChatUser?>(null)
    val chatUser: StateFlow<ChatUser?> = _chatUser

    fun listenForMessages(chatId: String) {
        viewModelScope.launch {
            listenMessagesUseCase(chatId).collectLatest { newMessages ->
                _messages.value = newMessages
                Log.d("ChatViewModel", "📩 Mesaj sayısı: ${newMessages.size}")
            }
        }
    }

    fun sendMessageToUser(chatId: String, messageText: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        // Receiver ID'yi chat ID'den al
        val receiverId = getReceiverIdFromChatId(chatId, currentUserId)

        val message = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId,  // Artık doğru receiverId'yi kullanıyoruz
            message = messageText,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            sendMessageUseCase(
                chatId,
                message,
                onSuccess = { Log.d("ChatViewModel", "✅ Mesaj gönderildi!") },
                onFailure = { e -> Log.e("ChatViewModel", "❌ Mesaj gönderme hatası!", e) }
            )
        }
    }

    fun fetchChatUserInfo(chatId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val otherUserId = getReceiverIdFromChatId(chatId, currentUserId)
                
                Log.d("ChatViewModel", "Fetching user info for ID: $otherUserId")
                
                firestore.collection("users")
                    .document(otherUserId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val userData = document.data
                            val photoUrl = userData?.get("photoUrl") as? String // Firebase'deki alan adını kontrol et
                            val name = userData?.get("name") as? String
                            
                            Log.d("ChatViewModel", "User data fetched - Name: $name, Photo: $photoUrl")
                            
                            _chatUser.value = ChatUser(
                                id = otherUserId,
                                name = name ?: "İsimsiz Kullanıcı",
                                profilePicture = photoUrl ?: ""
                            )
                        } else {
                            Log.e("ChatViewModel", "User document doesn't exist")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatViewModel", "Error fetching user data", e)
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception in fetchChatUserInfo", e)
            }
        }
    }

    // ChatId'den receiver ID'yi çıkaran fonksiyon
    private fun getReceiverIdFromChatId(chatId: String, currentUserId: String): String {
        val parts = chatId.split("_")
        return if (parts[0] == currentUserId) parts[1] else parts[0]
    }
}

data class ChatUser(
    val id: String,
    val name: String,
    val profilePicture: String
)