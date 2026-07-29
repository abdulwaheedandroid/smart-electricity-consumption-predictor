package com.abdulwaheed.smartelectricitypredictor.di

import com.abdulwaheed.smartelectricitypredictor.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Simple ServiceLocator used instead of Hilt for this project to provide Firebase instances
 * and repository singletons. This avoids adding kapt/ksp/hilt build-time plugins.
 */
object ServiceLocator {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val authRepository: AuthRepositoryImpl by lazy { AuthRepositoryImpl(auth) }
}

