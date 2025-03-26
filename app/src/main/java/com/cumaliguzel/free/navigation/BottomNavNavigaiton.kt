package com.cumaliguzel.free.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cumaliguzel.free.screens.ChatListPage
import com.cumaliguzel.free.screens.ChatScreen
import com.cumaliguzel.free.screens.MapsPage
import com.cumaliguzel.free.screens.PostScreen


@Composable
fun BottomNavNavigation(selectedPage: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = selectedPage
    ) {
        composable(route = "mapspage") { MapsPage(navController = navController) }
        composable(route = "postpage") { PostScreen(navController = navController) }
        composable(route = "post_screen") { PostScreen(navController = navController) }
        composable(route = "chatslistpage") {
            ChatListPage(navController = navController)
        }
        composable(route = "chatpage/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(navController = navController, chatId = chatId)
        }
    }

}
