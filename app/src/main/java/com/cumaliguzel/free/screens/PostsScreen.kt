package com.cumaliguzel.free.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cumaliguzel.free.components.PostCard
import com.cumaliguzel.free.viewmodel.PostViewModel
import com.cumaliguzel.free.viewmodel.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    navController: NavController,
    postViewModel: PostViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val posts by postViewModel.posts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                title = {
                    Text("İlanlar", style = MaterialTheme.typography.headlineSmall
                        ,color = MaterialTheme.colorScheme.tertiary
                        , fontWeight = FontWeight.Bold
                        )
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = paddingValues.calculateTopPadding(),
                    bottom = 0.dp // Bottom padding'i kaldırıyoruz
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 80.dp // Bottom nav için extra padding
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = posts,
                    key = { it.postId }
                ) { post ->
                    PostCard(
                        post = post,
                        postViewModel = postViewModel,
                        userViewModel = userViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

