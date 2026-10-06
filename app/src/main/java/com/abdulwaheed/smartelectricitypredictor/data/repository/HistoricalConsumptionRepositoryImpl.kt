package com.abdulwaheed.smartelectricitypredictor.data.repository

import androidx.room.withTransaction
import com.abdulwaheed.smartelectricitypredictor.data.local.dao.HistoricalConsumptionDao
import com.abdulwaheed.smartelectricitypredictor.data.local.database.AppDatabase
import com.abdulwaheed.smartelectricitypredictor.data.local.entity.HistoricalConsumptionEntity
import com.abdulwaheed.smartelectricitypredictor.data.local.mapper.toDomain
import com.abdulwaheed.smartelectricitypredictor.data.local.mapper.toEntity
import com.abdulwaheed.smartelectricitypredictor.domain.model.HistoricalConsumption
import com.abdulwaheed.smartelectricitypredictor.domain.repository.HistoricalConsumptionRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HistoricalConsumptionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val consumptionDao: HistoricalConsumptionDao,
    private val database: AppDatabase
) : HistoricalConsumptionRepository {

    private val syncMutex = Mutex()

    override fun observeHistoricalConsumption(
        uid: String
    ): Flow<List<HistoricalConsumption>> {
        requireValidUid(uid)

        return consumptionDao.observeHistoricalConsumption(uid)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun saveHistoricalConsumption(
        uid: String,
        consumption: HistoricalConsumption
    ): Result<Unit> = resultOf {
        requireValidUid(uid)
        validateConsumption(consumption)

        val recordId = consumptionId(
            year = consumption.year,
            month = consumption.month
        )

        require(consumption.id.isBlank() || consumption.id == recordId) {
            "Month and year cannot be changed for an existing record."
        }

        database.withTransaction {
            val existing = consumptionDao.getConsumptionForMonth(
                userId = uid,
                month = consumption.month,
                year = consumption.year
            )

            if (consumption.id.isBlank()) {
                require(existing == null || existing.isDeleted) {
                    "A record already exists for this month. Edit it instead."
                }
            } else {
                require(existing != null && !existing.isDeleted) {
                    "This record no longer exists. Add it again."
                }
            }

            require(existing == null || existing.id == recordId) {
                "The existing record has an unexpected ID."
            }

            val now = nextUpdatedAt(existing?.updatedAt)

            val record = consumption.copy(
                id = recordId,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )

            consumptionDao.upsertHistoricalConsumption(
                record.toEntity(
                    userId = uid,
                    isDeleted = false,
                    syncPending = true
                )
            )
        }
    }

    override suspend fun deleteHistoricalConsumption(
        uid: String,
        consumptionId: String
    ): Result<Unit> = resultOf {
        requireValidUid(uid)
        require(consumptionId.isNotBlank()) {
            "Record ID is required."
        }

        database.withTransaction {
            val existing = consumptionDao.getHistoricalConsumption(
                userId = uid,
                consumptionId = consumptionId
            ) ?: throw IllegalArgumentException("Record not found.")

            if (!existing.isDeleted) {
                consumptionDao.markDeleted(
                    userId = uid,
                    consumptionId = consumptionId,
                    updatedAt = nextUpdatedAt(existing.updatedAt)
                )
            }
        }
    }

    override suspend fun syncHistoricalConsumption(
        uid: String
    ): Result<Unit> = resultOf {
        requireValidUid(uid)

        syncMutex.withLock {
            var firstError: Exception? = null

            val pendingRecords =
                consumptionDao.getPendingSyncConsumption(uid)

            // Upload each pending record, including deletion markers.
            for (local in pendingRecords) {
                try {
                    val winner = reconcileWithServer(uid, local)
                    mergeIntoRoom(uid, winner)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    if (firstError == null) {
                        firstError = exception
                    }
                }
            }

            // Download records created or changed on other devices.
            try {
                val snapshots = records(uid)
                    .get(Source.SERVER)
                    .awaitCompletion()

                for (document in snapshots.documents) {
                    val remote = document.toHistoricalEntity(uid)
                    mergeIntoRoom(uid, remote)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (firstError == null) {
                    firstError = exception
                }
            }

            firstError?.let { throw it }
        }
    }

    private suspend fun reconcileWithServer(
        uid: String,
        local: HistoricalConsumptionEntity
    ): HistoricalConsumptionEntity {
        val reference = records(uid).document(local.id)

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(reference)

            val remote = if (snapshot.exists()) {
                snapshot.toHistoricalEntity(uid)
            } else {
                null
            }

            val localUpdatedAt = requireNotNull(local.updatedAt)
            val remoteUpdatedAt = remote?.updatedAt ?: 0L

            if (remote != null && remoteUpdatedAt > localUpdatedAt) {
                remote
            } else {
                // A pending local change wins an equal-timestamp conflict.
                transaction.set(reference, local.toFirestoreData())
                local.copy(syncPending = false)
            }
        }.awaitCompletion()
    }

    private suspend fun mergeIntoRoom(
        uid: String,
        remote: HistoricalConsumptionEntity
    ) {
        database.withTransaction {
            val current = consumptionDao.getHistoricalConsumption(
                userId = uid,
                consumptionId = remote.id
            )

            when {
                current == null -> {
                    consumptionDao.upsertHistoricalConsumption(
                        remote.copy(syncPending = false)
                    )
                }

                timestamp(remote) > timestamp(current) -> {
                    consumptionDao.upsertHistoricalConsumption(
                        remote.copy(syncPending = false)
                    )
                }

                timestamp(remote) == timestamp(current) &&
                        sameRecord(current, remote) -> {
                    // Only acknowledge the version that actually synced.
                    consumptionDao.updateSyncPending(
                        userId = uid,
                        consumptionId = current.id,
                        expectedUpdatedAt = requireNotNull(current.updatedAt),
                        syncPending = false
                    )
                }

                // A newer local change stays untouched and pending.
                // Equal-timestamp differing values also stay untouched.
                else -> Unit
            }
        }
    }

    private fun validateConsumption(
        consumption: HistoricalConsumption
    ) {
        require(consumption.month in 1..12) {
            "Select a valid month."
        }

        require(consumption.year in 1..9999) {
            "Enter a valid year."
        }

        require(
            consumption.unitsConsumed.isFinite() &&
                    consumption.unitsConsumed >= 0.0
        ) {
            "Units consumed must be a valid non-negative number."
        }

        require(
            consumption.billAmount.isFinite() &&
                    consumption.billAmount >= 0.0
        ) {
            "Bill amount must be a valid non-negative number."
        }

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        require(
            consumption.year < currentYear ||
                    (
                            consumption.year == currentYear &&
                                    consumption.month <= currentMonth
                            )
        ) {
            "Consumption cannot be entered for a future month."
        }
    }

    private fun DocumentSnapshot.toHistoricalEntity(
        uid: String
    ): HistoricalConsumptionEntity {
        fun requiredLong(field: String): Long {
            return getLong(field)
                ?: throw IllegalStateException(
                    "Historical record $id is missing $field."
                )
        }

        fun requiredNumber(field: String): Double {
            return (get(field) as? Number)?.toDouble()
                ?: throw IllegalStateException(
                    "Historical record $id is missing $field."
                )
        }

        val monthValue = requiredLong("month")
        val yearValue = requiredLong("year")
        val units = requiredNumber("unitsConsumed")
        val bill = requiredNumber("billAmount")

        require(monthValue in 1L..12L && yearValue in 1L..9999L) {
            "Historical record $id has an invalid period."
        }

        val month = monthValue.toInt()
        val year = yearValue.toInt()

        require(id == consumptionId(year, month)) {
            "Historical record $id does not match its period."
        }

        require(units.isFinite() && units >= 0.0) {
            "Historical record $id has invalid units."
        }

        require(bill.isFinite() && bill >= 0.0) {
            "Historical record $id has an invalid bill amount."
        }

        val createdAt = requiredLong("createdAt")
        val updatedAt = requiredLong("updatedAt")

        require(createdAt > 0L && updatedAt >= createdAt) {
            "Historical record $id has invalid timestamps."
        }

        return HistoricalConsumptionEntity(
            userId = uid,
            id = id,
            month = month,
            year = year,
            unitsConsumed = units,
            billAmount = bill,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = getBoolean("isDeleted") ?: false,
            syncPending = false
        )
    }

    private fun HistoricalConsumptionEntity.toFirestoreData():
            Map<String, Any?> {
        return mapOf(
            "month" to month,
            "year" to year,
            "unitsConsumed" to unitsConsumed,
            "billAmount" to billAmount,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "isDeleted" to isDeleted
        )
    }

    private fun sameRecord(
        first: HistoricalConsumptionEntity,
        second: HistoricalConsumptionEntity
    ): Boolean {
        return first.copy(syncPending = false) ==
                second.copy(syncPending = false)
    }

    private fun timestamp(
        entity: HistoricalConsumptionEntity
    ): Long = entity.updatedAt ?: 0L

    private fun nextUpdatedAt(previous: Long?): Long {
        val previousValue = previous ?: 0L
        check(previousValue < Long.MAX_VALUE) {
            "Record timestamp cannot be advanced."
        }

        return maxOf(
            System.currentTimeMillis(),
            previousValue + 1L
        )
    }

    private fun consumptionId(year: Int, month: Int): String {
        return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}"
    }

    private fun requireValidUid(uid: String) {
        require(uid.isNotBlank() && '/' !in uid) {
            "A valid user ID is required."
        }
    }

    private fun records(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection("historicalConsumption")

    private suspend fun resultOf(
        block: suspend () -> Unit
    ): Result<Unit> {
        return try {
            block()
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private suspend fun <T> Task<T>.awaitCompletion(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                if (continuation.isActive) {
                    when {
                        task.isCanceled -> {
                            continuation.cancel()
                        }

                        task.isSuccessful -> {
                            continuation.resume(task.result)
                        }

                        else -> {
                            continuation.resumeWithException(
                                task.exception
                                    ?: IllegalStateException(
                                        "Firebase operation failed."
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
