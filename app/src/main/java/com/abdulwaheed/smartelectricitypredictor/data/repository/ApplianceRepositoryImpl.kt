package com.abdulwaheed.smartelectricitypredictor.data.repository

import android.util.Log
import com.abdulwaheed.smartelectricitypredictor.data.local.dao.ApplianceDao
import com.abdulwaheed.smartelectricitypredictor.data.local.entity.ApplianceEntity
import com.abdulwaheed.smartelectricitypredictor.data.local.mapper.toDomain
import com.abdulwaheed.smartelectricitypredictor.data.local.mapper.toEntity
import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ApplianceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class ApplianceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val applianceDao: ApplianceDao
) : ApplianceRepository {

    override fun observeAppliances(uid: String): Flow<List<Appliance>> {
        return applianceDao.observeAppliances(uid).map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override suspend fun saveAppliance(
        uid: String,
        appliance: Appliance
    ): Result<Unit> = runCatching {

        val now = System.currentTimeMillis()

        val existingEntity = if (appliance.id.isNotBlank()) {
            applianceDao.getAppliance(
                userId = uid,
                applianceId = appliance.id
            )
        } else {
            null
        }

        val applianceId = appliance.id.ifBlank {
            appliances(uid).document().id
        }

        val applianceToSave = appliance.copy(
            id = applianceId,
            createdAt = existingEntity?.createdAt ?: now,
            updatedAt = now
        )

        applianceDao.upsertAppliance(
            applianceToSave.toEntity(
                userId = uid,
                syncPending = true
            )
        )

        try {
            saveApplianceToFirestore(
                uid = uid,
                appliance = applianceToSave
            )

            applianceDao.updateSyncPending(
                userId = uid,
                applianceId = applianceId,
                syncPending = false
            )
        } catch (exception: Exception) {
            Log.e(
                "ApplianceRepository",
                "Failed to sync appliance to Firestore",
                exception
            )
        }
    }

    override suspend fun deleteAppliance(
        uid: String,
        applianceId: String
    ): Result<Unit> = runCatching {

        val now = System.currentTimeMillis()

        applianceDao.markDeleted(
            userId = uid,
            applianceId = applianceId,
            updatedAt = now
        )

        try {
            deleteApplianceFromFirestore(
                uid = uid,
                applianceId = applianceId
            )

            applianceDao.updateSyncPending(
                userId = uid,
                applianceId = applianceId,
                syncPending = false
            )
        } catch (_: Exception) {
            // Local deletion succeeded.
            // Keep the tombstone with syncPending = true
            // so the Firestore deletion can be retried later.
        }
    }

    private suspend fun deleteApplianceFromFirestore(
        uid: String,
        applianceId: String
    ) {
        suspendCancellableCoroutine<Unit> { continuation ->

            appliances(uid)
                .document(applianceId)
                .delete()
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWith(
                            Result.failure(exception)
                        )
                    }
                }
        }
    }

    private suspend fun saveApplianceToFirestore(
        uid: String,
        appliance: Appliance
    ) {
        suspendCancellableCoroutine<Unit> { continuation ->

            val document = appliances(uid).document(appliance.id)

            val data = mapOf(
                NAME to appliance.name,
                POWER_WATTS to appliance.powerWatts,
                DAILY_USAGE_HOURS to appliance.dailyUsageHours,
                CREATED_AT to appliance.createdAt,
                UPDATED_AT to appliance.updatedAt
            )

            document.set(data)
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWith(
                            Result.failure(exception)
                        )
                    }
                }
        }
    }

    override suspend fun syncAppliances(
        uid: String
    ): Result<Unit> = runCatching {

        val localAppliances = applianceDao.getAllAppliances(uid)
        val remoteAppliances = getAppliancesFromFirestore(uid)

        val localById = localAppliances.associateBy { it.id }
        val remoteById = remoteAppliances.associateBy { it.id }

        val allIds = localById.keys + remoteById.keys

        var firstError: Exception? = null

        for (applianceId in allIds) {

            val local = localById[applianceId]
            val remote = remoteById[applianceId]

            try {
                when {
                    local == null && remote != null -> {
                        applianceDao.upsertAppliance(
                            remote.toEntity(
                                userId = uid,
                                isDeleted = false,
                                syncPending = false
                            )
                        )
                    }

                    local != null && remote == null -> {
                        if (local.syncPending) {

                            if (local.isDeleted) {
                                applianceDao.updateSyncPending(
                                    userId = uid,
                                    applianceId = applianceId,
                                    syncPending = false
                                )
                            } else {
                                saveApplianceToFirestore(
                                    uid = uid,
                                    appliance = local.toDomain()
                                )

                                applianceDao.updateSyncPending(
                                    userId = uid,
                                    applianceId = applianceId,
                                    syncPending = false
                                )
                            }
                        }
                    }

                    local != null && remote != null -> {
                        reconcileAppliance(
                            uid = uid,
                            local = local,
                            remote = remote
                        )
                    }
                }
            } catch (exception: Exception) {
                if (firstError == null) {
                    firstError = exception
                }
            }
        }

        firstError?.let {
            throw it
        }
    }

    private suspend fun reconcileAppliance(
        uid: String,
        local: ApplianceEntity,
        remote: Appliance
    ) {
        val localUpdatedAt = local.updatedAt ?: 0L
        val remoteUpdatedAt = remote.updatedAt ?: 0L

        if (local.isDeleted) {

            if (localUpdatedAt >= remoteUpdatedAt) {
                deleteApplianceFromFirestore(
                    uid = uid,
                    applianceId = local.id
                )

                applianceDao.updateSyncPending(
                    userId = uid,
                    applianceId = local.id,
                    syncPending = false
                )
            } else {
                applianceDao.upsertAppliance(
                    remote.toEntity(
                        userId = uid,
                        isDeleted = false,
                        syncPending = false
                    )
                )
            }

            return
        }

        when {
            localUpdatedAt > remoteUpdatedAt -> {
                saveApplianceToFirestore(
                    uid = uid,
                    appliance = local.toDomain()
                )

                applianceDao.updateSyncPending(
                    userId = uid,
                    applianceId = local.id,
                    syncPending = false
                )
            }

            remoteUpdatedAt > localUpdatedAt -> {
                applianceDao.upsertAppliance(
                    remote.toEntity(
                        userId = uid,
                        isDeleted = false,
                        syncPending = false
                    )
                )
            }

            else -> {
                if (local.syncPending) {
                    applianceDao.updateSyncPending(
                        userId = uid,
                        applianceId = local.id,
                        syncPending = false
                    )
                }
            }
        }
    }

    private suspend fun getAppliancesFromFirestore(
        uid: String
    ): List<Appliance> {
        return suspendCancellableCoroutine { continuation ->

            appliances(uid)
                .get()
                .addOnSuccessListener { snapshots ->

                    if (!continuation.isActive) {
                        return@addOnSuccessListener
                    }

                    try {
                        val appliances = snapshots.documents.map { document ->

                            Appliance(
                                id = document.id,

                                name = document.getString(NAME)
                                    ?: throw IllegalStateException(
                                        "Appliance name is missing"
                                    ),

                                powerWatts = document.getLong(POWER_WATTS)?.toInt()
                                    ?: throw IllegalStateException(
                                        "Appliance power rating is missing"
                                    ),

                                dailyUsageHours =
                                    document.getDouble(DAILY_USAGE_HOURS)
                                        ?: document.getLong(DAILY_USAGE_HOURS)?.toDouble()
                                        ?: throw IllegalStateException(
                                            "Appliance daily usage is missing"
                                        ),

                                createdAt = document.getLong(CREATED_AT),

                                updatedAt = document.getLong(UPDATED_AT)
                            )
                        }

                        continuation.resume(appliances)

                    } catch (exception: Exception) {
                        continuation.resumeWith(
                            Result.failure(exception)
                        )
                    }
                }
                .addOnFailureListener { exception ->

                    if (continuation.isActive) {
                        continuation.resumeWith(
                            Result.failure(exception)
                        )
                    }
                }
        }
    }

    private fun appliances(uid: String) = firestore.collection(USERS_COLLECTION)
        .document(uid)
        .collection(APPLIANCES_COLLECTION)

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
