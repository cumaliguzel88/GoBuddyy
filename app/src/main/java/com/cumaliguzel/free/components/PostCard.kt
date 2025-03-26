package com.cumaliguzel.free.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.cumaliguzel.free.data.model.Post
import com.cumaliguzel.free.viewmodel.PostViewModel
import com.cumaliguzel.free.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PostCard(
    post: Post,
    postViewModel: PostViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val userInfo = remember { mutableStateOf<String?>(null) }
    val userPhoto = remember { mutableStateOf<String?>(null) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(post.userId) {
        if (post.userId.isNotEmpty()) {
            userViewModel.fetchUserDetails(post.userId) { user ->
                userInfo.value = user.name
                userPhoto.value = user.photoUrl
            }
        }
    }

    val remainingTime by postViewModel.remainingTimeMap[post.postId]?.collectAsState() ?: remember { mutableStateOf(0L) }
    val formattedTime = postViewModel.formatTime(remainingTime)

    val displayName = userInfo.value ?: post.username
    val displayPhotoUrl = userPhoto.value ?: post.userPhotoUrl

    val timerColor = remember(remainingTime) {
        when {
            remainingTime > 900 -> Color(0xFF4CAF50)
            remainingTime in 301..900 -> Color(0xFFFFF176)
            else -> Color(0xFFF44336)
        }
    }

    val filteredPrices = post.estimatedPrice
        .split(", ")
        .filter { price -> post.transportType.any { type -> price.contains(type) } }
        .map { price ->
            val parts = price.split(": ")
            if (parts.size == 2) {
                val transportType = parts[0]
                val priceValue = postViewModel.formatEstimatedPrice(parts[1])
                "$transportType: $priceValue "
            } else price
        }
        .joinToString("\n")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = displayPhotoUrl,
                            contentDescription = "Profil Fotoğrafı",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = timerColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(label = "\uD83D\uDCCDNereden", value = post.fromLocation)
                InfoRow(label = "\uD83D\uDEE3\uFE0FNereye", value = post.toLocation)

                if (post.transportType.isNotEmpty()) {
                    InfoRow(label = "\uD83D\uDE97Tercih Edilen Ulaşım", value = post.transportType.joinToString(", "))
                }

                if (filteredPrices.isNotEmpty()) {
                    Text(
                        text = "\uD83D\uDE95Seçilen Ulaşım Araçları Tahmini Ücret:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = filteredPrices,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 📌 **Mesafe ve Tahmini Süreyi Gösterme**
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "\uD83D\uDCCF Mesafe: ${if (post.distanceKm.isNotEmpty()) post.distanceKm else "Bilinmiyor"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    )

                    Text(
                        text = "\uD83D\uDD52${if (post.estimatedDuration.isNotEmpty()) post.estimatedDuration else "Bilinmiyor"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Oluşturulma Zamanı: ${postViewModel.formatDate(post.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )

                if (post.userId != currentUserId) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                val chatId = if (currentUserId!! < post.userId) {
                                    "${currentUserId}_${post.userId}"
                                } else {
                                    "${post.userId}_${currentUserId}"
                                }
                                navController.navigate("chatpage/$chatId")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text(text = "Mesaj Gönder", color = Color.White)
                        }
                    }
                }
            }

            if (post.userId == currentUserId) {
                Text(
                    text = "OWNER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xFF4CAF50), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}