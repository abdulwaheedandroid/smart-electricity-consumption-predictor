package com.abdulwaheed.smartelectricitypredictor.data.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.UserProfile
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ProfileRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfileRepository {
    override suspend fun getProfile(uid: String): Result<UserProfile?> = runCatching {
        suspendCancellableCoroutine { continuation ->
            firestore.collection(USERS_COLLECTION).document(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    if (!snapshot.exists()) {
                        continuation.resume(null)
                        return@addOnSuccessListener
                    }

                    try {
                        continuation.resume(
                            UserProfile(
                                uid = snapshot.id,
                                fullName = snapshot.getString(FULL_NAME).orEmpty(),
                                email = snapshot.getString(EMAIL).orEmpty(),
                                age = snapshot.getLong(AGE)?.toInt()
                                    ?: throw IllegalStateException("Profile age is missing"),
                                gender = snapshot.getString(GENDER).orEmpty(),
                                cellNumber = snapshot.getString(CELL_NUMBER).orEmpty(),
                                createdAt = snapshot.getTimestamp(CREATED_AT)?.toEpochMillis(),
                                updatedAt = snapshot.getTimestamp(UPDATED_AT)?.toEpochMillis()
                            )
                        )
                    } catch (exception: Exception) {
                        continuation.resumeWith(Result.failure(exception))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                }
        }
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> = runCatching {
        suspendCancellableCoroutine { continuation ->
            val document = firestore.collection(USERS_COLLECTION).document(profile.uid)
            firestore.runTransaction { transaction ->
                val existingDocument = transaction.get(document)
                val profileData = mutableMapOf<String, Any>(
                    UID to profile.uid,
                    FULL_NAME to profile.fullName,
                    EMAIL to profile.email,
                    AGE to profile.age,
                    GENDER to profile.gender,
                    CELL_NUMBER to profile.cellNumber,
                    UPDATED_AT to FieldValue.serverTimestamp()
                )
                if (!existingDocument.exists()) {
                    profileData[CREATED_AT] = FieldValue.serverTimestamp()
                } else {
                    existingDocument.getTimestamp(CREATED_AT)?.let {
                        profileData[CREATED_AT] = it
                    }
                }
                transaction.set(document, profileData)
            }.addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Unit)
            }.addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
            }
        }
    }

    override suspend fun deleteProfile(uid: String): Result<Unit> = runCatching {
        suspendCancellableCoroutine { continuation ->
            firestore.collection(USERS_COLLECTION).document(uid).delete()
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.failure(exception))
                    }
                }
        }
    }

    private fun Timestamp.toEpochMillis(): Long = toDate().time

    private companion object {
        const val USERS_COLLECTION = "users"
        const val UID = "uid"
        const val FULL_NAME = "fullName"
        const val EMAIL = "email"
        const val AGE = "age"
        const val GENDER = "gender"
        const val CELL_NUMBER = "cellNumber"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
    }
}
