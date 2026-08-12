package com.abdulwaheed.smartelectricitypredictor.data.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ApplianceRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class ApplianceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ApplianceRepository {
    override suspend fun getAppliances(uid: String): Result<List<Appliance>> = runCatching {
        suspendCancellableCoroutine { continuation ->
            appliances(uid).orderBy(NAME).get()
                .addOnSuccessListener { snapshots ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    try {
                        continuation.resume(snapshots.documents.map { snapshot ->
                            Appliance(
                                id = snapshot.id,
                                name = snapshot.getString(NAME)
                                    ?: throw IllegalStateException("Appliance name is missing"),
                                powerWatts = snapshot.getLong(POWER_WATTS)?.toInt()
                                    ?: throw IllegalStateException("Appliance power rating is missing"),
                                dailyUsageHours = snapshot.getDouble(DAILY_USAGE_HOURS)
                                    ?: snapshot.getLong(DAILY_USAGE_HOURS)?.toDouble()
                                    ?: throw IllegalStateException("Appliance daily usage is missing"),
                                createdAt = snapshot.getTimestamp(CREATED_AT)?.toEpochMillis(),
                                updatedAt = snapshot.getTimestamp(UPDATED_AT)?.toEpochMillis()
                            )
                        })
                    } catch (exception: Exception) {
                        continuation.resumeWith(Result.failure(exception))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                }
        }
    }

    override suspend fun saveAppliance(uid: String, appliance: Appliance): Result<Unit> = runCatching {
        suspendCancellableCoroutine { continuation ->
            val document = if (appliance.id.isBlank()) appliances(uid).document() else appliances(uid).document(appliance.id)
            firestore.runTransaction { transaction ->
                val existingDocument = transaction.get(document)
                val data = mutableMapOf<String, Any>(
                    NAME to appliance.name,
                    POWER_WATTS to appliance.powerWatts,
                    DAILY_USAGE_HOURS to appliance.dailyUsageHours,
                    UPDATED_AT to FieldValue.serverTimestamp()
                )
                if (!existingDocument.exists()) {
                    data[CREATED_AT] = FieldValue.serverTimestamp()
                } else {
                    existingDocument.getTimestamp(CREATED_AT)?.let { data[CREATED_AT] = it }
                }
                transaction.set(document, data)
            }.addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Unit)
            }.addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
            }
        }
    }

    override suspend fun deleteAppliance(uid: String, applianceId: String): Result<Unit> = runCatching {
        suspendCancellableCoroutine { continuation ->
            appliances(uid).document(applianceId).delete()
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(Unit) }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                }
        }
    }

    private fun appliances(uid: String) = firestore.collection(USERS_COLLECTION)
        .document(uid)
        .collection(APPLIANCES_COLLECTION)

    private fun Timestamp.toEpochMillis(): Long = toDate().time

    private companion object {
        const val USERS_COLLECTION = "users"
        const val APPLIANCES_COLLECTION = "appliances"
        const val NAME = "name"
        const val POWER_WATTS = "powerWatts"
        const val DAILY_USAGE_HOURS = "dailyUsageHours"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
    }
}
