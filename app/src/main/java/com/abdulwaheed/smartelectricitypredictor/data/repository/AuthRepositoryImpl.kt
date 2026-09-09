package com.abdulwaheed.smartelectricitypredictor.data.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.User
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val auth: FirebaseAuth) : AuthRepository {
    override fun getCurrentUser(): User? = auth.currentUser?.let { User(it.uid, it.email) }
    override suspend fun signOut(): Result<Unit> = runCatching { auth.signOut() }
}
