package com.example.culinarycompanion

import android.app.Application
import com.google.firebase.FirebaseApp

class CulinaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}