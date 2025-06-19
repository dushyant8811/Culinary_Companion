package com.example.culinarycompanion.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    // Handle authentication result
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authViewModel.handleSignInResult(result.data)
        }
    }

    // Initialize auth on first launch
    LaunchedEffect(Unit) {
        authViewModel.initialize(context)
        authViewModel.checkAuthState()
    }

    // Handle successful login
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (user.displayName?.isNotEmpty() == true) {
                navController.navigate("recipeList") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                navController.navigate("profileSetup")
            }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Welcome to Culinary Companion",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )

                    Button(
                        onClick = {
                            authViewModel.getSignInIntent()?.let {
                                launcher.launch(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign in with Google")
                    }

                    OutlinedButton(
                        onClick = { /* Implement email login later */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign in with Email")
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}