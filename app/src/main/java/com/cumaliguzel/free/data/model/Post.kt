package com.cumaliguzel.free.data.model

import com.google.firebase.Timestamp

data class Post(
    val postId: String = "",
    val userId: String = "",           // 🔥 Postu atan kişinin ID'si
    val username: String = "",         // 🔥 Postu atan kişinin adı
    val userPhotoUrl: String = "",     // 🔥 Postu atan kişinin profil fotoğrafı
    val fromLocation: String = "",
    val toLocation: String = "",
    val transportType: List<String> = emptyList(),
    val estimatedPrice: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val distanceKm: String = "",  // 🔥 Eklenen Mesafe Bilgisi (km)
    val estimatedDuration: String = "",  // 🔥 Eklenen Süre Bilgisi (dakika)
    val deleteAt: Timestamp = Timestamp(Timestamp.now().seconds + 30 * 60, 0)
)