package com.cumaliguzel.free.data.model

data class ChatSummary(
    val chatId: String = "",
    val users: Map<String, Boolean> = emptyMap(), // {userA:true, userB:true}
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val otherUserName: String = "",
    val otherUserPhotoUrl: String = ""
)