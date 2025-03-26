package com.cumaliguzel.free

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cumaliguzel.free.navigation.BottomNavBar
import com.cumaliguzel.free.screens.GoogleSignInPage
import com.cumaliguzel.free.ui.theme.FreeTheme
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 📌 Google Places API'yi başlatma
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCxC3gxRm4aRKISlwgp0aUXWOIgktXFizY")
        }
        enableEdgeToEdge()
        setContent {
            FreeTheme {
                val navController = rememberNavController()
                val currentUser = Firebase.auth.currentUser

                NavHost(
                    navController = navController,
                    startDestination = if (currentUser != null) "main" else "signinpage"
                ) {
                    composable("signinpage") {
                        GoogleSignInPage(
                            navController = navController
                        )
                    }
                    composable("main") {
                        BottomNavBar()
                    }
                }
            }
        }
    }
}



