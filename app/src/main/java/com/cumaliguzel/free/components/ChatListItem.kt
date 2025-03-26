package com.cumaliguzel.free.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cumaliguzel.free.data.model.ChatSummary
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bu composable ın yaratılma amacı whatsapp gibi listelemke ve kullanıya son konuşmaları sunmak
 * Bu composable beslediği sayfa ChatListPage
 */

@Composable
fun ChatListItem(
    chat: ChatSummary,
    onChatClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
            .clickable { onChatClick(chat.chatId) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sol taraf - Profil ve mesaj bilgileri
            Row(
                modifier = Modifier.weight(1f)
            ) {
                // Profil fotoğrafı
                AsyncImage(
                    model = chat.otherUserPhotoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // İsim ve son mesaj
                Column {
                    Text(
                        text = chat.otherUserName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Sağ taraf - Zaman
            Text(
                text = formatLastMessageTime(chat.lastMessageTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private fun formatLastMessageTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        // Bugün içindeyse saat
        diff < 24 * 60 * 60 * 1000 -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        // Dün
        diff < 48 * 60 * 60 * 1000 -> {
            "Dün"
        }
        // Son 7 gün içindeyse gün adı
        diff < 7 * 24 * 60 * 60 * 1000 -> {
            SimpleDateFormat("EEEE", Locale("tr")).format(Date(timestamp))
        }
        // Daha eski ise tarih
        else -> {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}