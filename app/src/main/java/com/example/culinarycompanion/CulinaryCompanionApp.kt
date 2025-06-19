package com.example.culinarycompanion

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp

class CulinaryCompanionApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase early
        FirebaseApp.initializeApp(this)
    }
}