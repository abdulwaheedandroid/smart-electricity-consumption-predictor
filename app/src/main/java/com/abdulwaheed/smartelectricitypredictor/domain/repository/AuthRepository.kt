package com.abdulwaheed.smartelectricitypredictor.domain.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.User

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun signOut(): Result<Unit>
}

