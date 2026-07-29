package com.abdulwaheed.smartelectricitypredictor

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Explicit Firebase initialization (google-services plugin often does this automatically,
        // but explicit call ensures deterministic behavior)
        FirebaseApp.initializeApp(this)
    }
}

