package com.cumaliguzel.free.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cumaliguzel.free.R
import com.cumaliguzel.free.auth.GoogleSignInUtils
import com.cumaliguzel.free.components.GoogleSignInButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun GoogleSignInPage(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser = Firebase.auth.currentUser

    // 📌 Kullanıcı zaten giriş yaptıysa direkt ana ekrana yönlendir
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("main") {
                popUpTo("signinpage") { inclusive = true }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        GoogleSignInUtils.doGoogleSignIn(
            context = context,
            scope = scope,
            launcher = null,
            login = {
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                navController.navigate("main") {
                    popUpTo("signinpage") { inclusive = true } // Geriye dönmeyi engelle
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


       // google sign in Button with row
        GoogleSignInButton(
            navController = navController,
            launcher = launcher,
            modifier = Modifier.padding(8.dp)
        )
    }
}