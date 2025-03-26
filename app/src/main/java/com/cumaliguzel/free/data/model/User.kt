package com.cumaliguzel.free.data.model

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Timestamp = Timestamp.now()
)