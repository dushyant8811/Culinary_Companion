package com.example.culinarycompanion.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.culinarycompanion.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var googleSignInClient: GoogleSignInClient? = null

    fun initialize(context: Context) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Initialization failed", e)
            _errorMessage.value = "Authentication service unavailable"
        }
    }

    fun getSignInIntent() = googleSignInClient?.signInIntent

    fun handleSignInResult(data: Intent?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                account?.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Sign-in failed", e)
                _errorMessage.value = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign in cancelled"
                    GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign in failed"
                    else -> "Authentication failed: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            _currentUser.value = auth.currentUser
            _errorMessage.value = null
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Authentication failed", e)
            _errorMessage.value = "Authentication failed: ${e.message}"
        }
    }

    fun checkAuthState() {
        _currentUser.value = auth.currentUser
    }

    fun updateProfile(name: String) {
        viewModelScope.launch {
            try {
                // FIXED: Properly create profile update request
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                auth.currentUser?.updateProfile(profileUpdates)?.await()
                _currentUser.value = auth.currentUser
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Profile update failed", e)
                _errorMessage.value = "Profile update failed: ${e.message}"
            }
        }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}