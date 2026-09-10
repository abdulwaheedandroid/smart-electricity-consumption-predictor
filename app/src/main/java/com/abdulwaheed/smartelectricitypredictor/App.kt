package com.abdulwaheed.smartelectricitypredictor

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply the supported light mode to FirebaseUI's AppCompat activities too.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Explicit Firebase initialization (google-services plugin often does this automatically,
        // but explicit call ensures deterministic behavior)
        FirebaseApp.initializeApp(this)
    }
}

