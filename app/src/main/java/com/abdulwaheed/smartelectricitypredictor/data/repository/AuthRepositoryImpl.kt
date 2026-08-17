package com.abdulwaheed.smartelectricitypredictor.data.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.User
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override fun getCurrentUser(): User? {
        val u = auth.currentUser
        return if (u != null) User(u.uid, u.email) else null
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

