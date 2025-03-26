package com.cumaliguzel.free.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cumaliguzel.free.R

@Composable
fun BottomNavBar() {
    val selectedItem = remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(110.dp),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                tonalElevation = 10.dp
            ) {
                NavigationBarItem(
                    selected = selectedItem.value == 0,
                    onClick = { selectedItem.value = 0 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.maps_icon),
                            contentDescription = "Maps Icon",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    },
                    label = { Text(text = "Maps", fontWeight = FontWeight.Bold) },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                        selectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )

                NavigationBarItem(
                    selected = selectedItem.value == 1,
                    onClick = { selectedItem.value = 1 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.post_icon),
                            contentDescription = "Post Icon",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    },
                    label = { Text(text = "Post", fontWeight = FontWeight.Bold) },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                        selectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )

                NavigationBarItem(
                    selected = selectedItem.value == 2,
                    onClick = { selectedItem.value = 2 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.chat_icon),
                            contentDescription = "Chat Icon",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    },
                    label = { Text(text = "Chat", fontWeight = FontWeight.Bold) },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                        selectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (selectedItem.value) {
                0 -> BottomNavNavigation("mapspage")
                1 -> BottomNavNavigation("postpage")
                2 -> BottomNavNavigation("chatslistpage")
            }
        }
    }
}