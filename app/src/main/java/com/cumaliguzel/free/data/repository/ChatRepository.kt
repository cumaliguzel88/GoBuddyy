package com.cumaliguzel.free.data.repository

import android.util.Log
import com.cumaliguzel.free.data.model.ChatMessage
import com.cumaliguzel.free.data.model.ChatSummary
import com.cumaliguzel.free.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val messagesRef = realtimeDatabase.getReference("messages")
    private val chatsRef = realtimeDatabase.getReference("chats")

    /**
     * 🔥 Yeni mesaj gönderir.
     *  1) /messages/{chatId}/ altında mesaj kaydeder.
     *  2) /chats/{chatId} altında son mesaj & kullanıcı bilgilerini günceller.
     */
    fun sendMessage(
        chatId: String,
        message: ChatMessage,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Sohbet özetini oluştur veya güncelle
        createOrUpdateChat(chatId, message)

        // Mesajı /messages/{chatId} altına yaz
        val messageKey = messagesRef.child(chatId).push().key ?: return
        messagesRef.child(chatId).child(messageKey).setValue(message)
            .addOnSuccessListener {
                Log.d("ChatRepository", "✅ Mesaj gönderildi: ${message.message}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ChatRepository", "❌ Mesaj gönderme başarısız!", e)
                onFailure(e)
            }
    }

    /**
     * 🔥 İlgili chatId altındaki mesajları anlık olarak dinler.
     */
    fun listenForMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val query = messagesRef.child(chatId).orderByChild("timestamp")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = snapshot.children.mapNotNull { data ->
                    data.getValue(ChatMessage::class.java)
                }
                trySend(messageList).isSuccess
                Log.d("ChatRepository", "📩 Mesaj sayısı: ${messageList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "❌ Mesaj dinleme başarısız!", error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    /**
     * 🔥 Kullanıcının dahil olduğu sohbetleri getirir.
     *    /chats düğümünde "users/currentUserId" = true olan kayıtları çekiyoruz.
     */
    fun fetchUserChats(): Flow<List<ChatSummary>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow
        
        Log.d("ChatRepository", "🔍 Sohbetler alınıyor... CurrentUserId: $currentUserId")
        
        val query = chatsRef.orderByChild("users/$currentUserId").equalTo(true)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Log.d("ChatRepository", "📥 Veri geldi. Snapshot: ${snapshot.childrenCount}")
                    
                    val chatList = mutableListOf<ChatSummary>()
                    var processedChats = 0
                    
                    for (chatSnap in snapshot.children) {
                        val chatId = chatSnap.key ?: continue
                        Log.d("ChatRepository", "📝 Chat ID: $chatId işleniyor")
                        
                        val usersMap = chatSnap.child("users").getValue(
                            object : GenericTypeIndicator<Map<String, Boolean>>() {}
                        ) ?: emptyMap()
                        
                        // Diğer kullanıcının ID'sini bul
                        val otherUserId = usersMap.keys.find { it != currentUserId } ?: continue
                        
                        // Firestore'dan kullanıcı bilgilerini al
                        firestore.collection("users").document(otherUserId).get()
                            .addOnSuccessListener { document ->
                                val otherUser = document.toObject(User::class.java)
                                Log.d("ChatRepository", "👤 Kullanıcı bilgileri alındı: ${otherUser?.name}")
                                
                                chatList.add(
                                    ChatSummary(
                                        chatId = chatId,
                                        users = usersMap,
                                        lastMessage = chatSnap.child("lastMessage").getValue(String::class.java) ?: "",
                                        lastMessageTimestamp = chatSnap.child("lastMessageTimestamp").getValue(Long::class.java) ?: 0L,
                                        otherUserName = otherUser?.name ?: "Bilinmeyen Kullanıcı",
                                        otherUserPhotoUrl = otherUser?.photoUrl ?: ""
                                    )
                                )
                                
                                processedChats++
                                Log.d("ChatRepository", "✅ İşlenen sohbet: $processedChats / ${snapshot.childrenCount}")
                                
                                if (processedChats == snapshot.childrenCount.toInt()) {
                                    val sortedList = chatList.sortedByDescending { it.lastMessageTimestamp }
                                    Log.d("ChatRepository", "🔄 Tüm sohbetler işlendi, liste gönderiliyor: ${sortedList.size}")
                                    trySend(sortedList)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("ChatRepository", "❌ Kullanıcı bilgileri alınamadı: $otherUserId", e)
                                processedChats++
                                if (processedChats == snapshot.childrenCount.toInt()) {
                                    val sortedList = chatList.sortedByDescending { it.lastMessageTimestamp }
                                    trySend(sortedList)
                                }
                            }
                    }
                    
                } catch (e: Exception) {
                    Log.e("ChatRepository", "❌ Veri parse hatası", e)
                    close(e)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "❌ Veri alma hatası: ${error.message}", error.toException())
                close(error.toException())
            }
        }
        
        query.addValueEventListener(listener)
        awaitClose { 
            Log.d("ChatRepository", "🔄 Listener kaldırılıyor")
            query.removeEventListener(listener) 
        }
    }
    /**
     * 🔥 Sohbet yoksa oluşturur, varsa günceller.
     *    /chats/{chatId}/users -> {senderId: true, receiverId: true}
     *    /chats/{chatId}/lastMessage -> "..."
     *    /chats/{chatId}/lastMessageTimestamp -> 12345
     */
    private fun createOrUpdateChat(chatId: String, message: ChatMessage) {
        // Önce chat'in var olup olmadığını kontrol et
        chatsRef.child(chatId).get().addOnSuccessListener { snapshot ->
            val updates = mutableMapOf<String, Any>()
            
            // Chat yoksa tüm yapıyı oluştur
            if (!snapshot.exists()) {
                updates["users/${message.senderId}"] = true
                updates["users/${message.receiverId}"] = true
            }
            
            // Son mesaj bilgilerini güncelle
            updates["lastMessage"] = message.message
            updates["lastMessageTimestamp"] = message.timestamp

            chatsRef.child(chatId).updateChildren(updates)
                .addOnSuccessListener {
                    Log.d("ChatRepository", "🔄 Sohbet özet bilgisi güncellendi: $chatId")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatRepository", "❌ Sohbet güncelleme hatası!", e)
                }
        }
    }

    fun listenForChats(): Flow<List<ChatSummary>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow
        
        Log.d("ChatRepository", "🔍 Sohbetler alınıyor... CurrentUserId: $currentUserId")
        
        // Kullanıcının tüm sohbetlerini getir
        val query = chatsRef.orderByChild("users/$currentUserId").equalTo(true)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Log.d("ChatRepository", "📥 Veri geldi. Snapshot: ${snapshot.childrenCount}")
                    
                    if (!snapshot.exists()) {
                        trySend(emptyList())
                        return
                    }

                    val chatList = mutableListOf<ChatSummary>()
                    var processedChats = 0
                    val totalChats = snapshot.childrenCount.toInt()
                    
                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key ?: continue
                        Log.d("ChatRepository", "📝 Chat ID: $chatId işleniyor")
                        
                        // Chat verilerini al
                        val lastMessage = chatSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                        val lastMessageTimestamp = chatSnapshot.child("lastMessageTimestamp").getValue(Long::class.java) ?: 0L
                        
                        // Diğer kullanıcının ID'sini bul
                        val otherUserId = chatId.split("_").firstOrNull { it != currentUserId } ?: continue
                        
                        // Firestore'dan diğer kullanıcının bilgilerini al
                        firestore.collection("users").document(otherUserId).get()
                            .addOnSuccessListener { userDoc ->
                                val otherUser = userDoc.toObject(User::class.java)
                                if (otherUser != null) {
                                    Log.d("ChatRepository", "👤 Kullanıcı bilgileri alındı: ${otherUser.name}")
                                    
                                    chatList.add(
                                        ChatSummary(
                                            chatId = chatId,
                                            otherUserName = otherUser.name,
                                            otherUserPhotoUrl = otherUser.photoUrl,
                                            lastMessage = lastMessage,
                                            lastMessageTimestamp = lastMessageTimestamp
                                        )
                                    )
                                }
                                
                                processedChats++
                                Log.d("ChatRepository", "✅ İşlenen sohbet: $processedChats / $totalChats")
                                
                                if (processedChats == totalChats) {
                                    val sortedList = chatList.sortedByDescending { it.lastMessageTimestamp }
                                    Log.d("ChatRepository", "🔄 Tüm sohbetler işlendi, liste gönderiliyor: ${sortedList.size}")
                                    trySend(sortedList)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("ChatRepository", "❌ Kullanıcı bilgileri alınamadı", e)
                                processedChats++
                                if (processedChats == totalChats) {
                                    val sortedList = chatList.sortedByDescending { it.lastMessageTimestamp }
                                    trySend(sortedList)
                                }
                            }
                    }
                } catch (e: Exception) {
                    Log.e("ChatRepository", "❌ Veri işleme hatası", e)
                    close(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "❌ Veri alma hatası: ${error.message}", error.toException())
                close(error.toException())
            }
        }
        
        query.addValueEventListener(listener)
        awaitClose { 
            Log.d("ChatRepository", "🔄 Listener kaldırılıyor")
            query.removeEventListener(listener) 
        }
    }
}