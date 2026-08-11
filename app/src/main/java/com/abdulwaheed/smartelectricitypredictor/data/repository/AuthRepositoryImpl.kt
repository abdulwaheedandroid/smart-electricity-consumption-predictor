package com.abdulwaheed.smartelectricitypredictor.data.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.User
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override fun getCurrentUser(): User? {
        val u = auth.currentUser
        return if (u != null) User(u.uid, u.email) else null
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val task = suspendCancellableCoroutine { cont ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val fu = authResult.user
                        if (fu != null) {
                            cont.resume(User(fu.uid, fu.email))
                        } else {
                            cont.resumeWithException(IllegalStateException("No user returned"))
                        }
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        return try {
            val task = suspendCancellableCoroutine { cont ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val fu = authResult.user
                        if (fu != null) {
                            cont.resume(User(fu.uid, fu.email))
                        } else {
                            cont.resumeWithException(IllegalStateException("No user returned"))
                        }
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

