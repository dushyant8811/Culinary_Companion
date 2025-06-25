package com.example.culinarycompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culinarycompanion.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onProfileComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    // Pre-fill name if available from auth
    LaunchedEffect(currentUser) {
        currentUser?.displayName?.let { displayName ->
            if (displayName.isNotBlank()) {
                name = displayName
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Set Up Your Profile",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            saveProfile(
                                name = name,
                                authViewModel = authViewModel,
                                firestore = firestore,
                                userId = currentUser?.uid,
                                coroutineScope = coroutineScope,
                                onComplete = onProfileComplete
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && !isLoading
                ) {
                    Text("Save Profile")
                }
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

private fun saveProfile(
    name: String,
    authViewModel: AuthViewModel,
    firestore: FirebaseFirestore,
    userId: String?,
    coroutineScope: CoroutineScope,
    onComplete: () -> Unit
) {
    coroutineScope.launch {
        try {
            // Update Firebase Auth profile
            authViewModel.updateProfile(name)

            // Save to Firestore if user is authenticated
            userId?.let { uid ->
                firestore.collection("users").document(uid)
                    .set(
                        mapOf(
                            "displayName" to name,
                            "profileComplete" to true,
                            "lastUpdated" to System.currentTimeMillis()
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        authViewModel.setErrorMessage("Failed to save profile: ${e.message}")
                    }
            } ?: run {
                onComplete() // Complete even if no userId (for testing)
            }
        } catch (e: Exception) {
            authViewModel.setErrorMessage("Profile setup failed: ${e.message}")
        }
    }
}