package com.abdulwaheed.smartelectricitypredictor.domain.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.User

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
}

