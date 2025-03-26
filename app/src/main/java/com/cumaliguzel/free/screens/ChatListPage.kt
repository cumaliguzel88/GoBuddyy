package com.cumaliguzel.free.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cumaliguzel.free.R
import com.cumaliguzel.free.components.ChatListItem
import com.cumaliguzel.free.viewmodel.ChatListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListPage(navController: NavController, viewModel: ChatListViewModel = hiltViewModel()) {
    val chatList by viewModel.chatList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isInitialLoad by viewModel.isInitialLoad.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchChatList()
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                title = { Text("Sohbetler", color = MaterialTheme.colorScheme.tertiary
                , fontWeight = FontWeight.Bold
                ) },
                actions = {
                    IconButton(onClick = { viewModel.refreshChatList() }) {
                        Icon(Icons.Default.Refresh, "Yenile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isInitialLoad -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                chatList.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz sohbet bulunmuyor",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "İlanlardan mesaj göndererek sohbet başlatabilirsiniz",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(chatList) { chat ->
                            ChatListItem(chat) {
                                navController.navigate("chatpage/${chat.chatId}")
                            }
                            if (isLoading) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
