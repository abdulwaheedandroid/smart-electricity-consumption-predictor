package com.abdulwaheed.smartelectricitypredictor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abdulwaheed.smartelectricitypredictor.data.local.entity.HistoricalConsumptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricalConsumptionDao {

    @Query(
        """
        SELECT * FROM historical_consumption
        WHERE userId = :userId
        AND isDeleted = 0
        ORDER BY year DESC, month DESC
        """
    )
    fun observeHistoricalConsumption(
        userId: String
    ): Flow<List<HistoricalConsumptionEntity>>

    @Query(
        """
        SELECT * FROM historical_consumption
        WHERE userId = :userId
        AND id = :consumptionId
        LIMIT 1
        """
    )
    suspend fun getHistoricalConsumption(
        userId: String,
        consumptionId: String
    ): HistoricalConsumptionEntity?

    @Query(
        """
        SELECT * FROM historical_consumption
        WHERE userId = :userId
        AND month = :month
        AND year = :year
        LIMIT 1
        """
    )
    suspend fun getConsumptionForMonth(
        userId: String,
        month: Int,
        year: Int
    ): HistoricalConsumptionEntity?

    @Upsert
    suspend fun upsertHistoricalConsumption(
        consumption: HistoricalConsumptionEntity
    )

    @Upsert
    suspend fun upsertHistoricalConsumptions(
        consumptions: List<HistoricalConsumptionEntity>
    )

    @Query(
        """
        UPDATE historical_consumption
        SET isDeleted = 1,
            syncPending = 1,
            updatedAt = :updatedAt
        WHERE userId = :userId
        AND id = :consumptionId
        """
    )
    suspend fun markDeleted(
        userId: String,
        consumptionId: String,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE historical_consumption
        SET syncPending = :syncPending
        WHERE userId = :userId
        AND id = :consumptionId
        AND updatedAt = :expectedUpdatedAt
        """
    )
    suspend fun updateSyncPending(
        userId: String,
        consumptionId: String,
        expectedUpdatedAt: Long,
        syncPending: Boolean
    ): Int

    @Query(
        """
        SELECT * FROM historical_consumption
        WHERE userId = :userId
        AND syncPending = 1
        """
    )
    suspend fun getPendingSyncConsumption(
        userId: String
    ): List<HistoricalConsumptionEntity>

    @Query(
        """
        SELECT * FROM historical_consumption
        WHERE userId = :userId
        """
    )
    suspend fun getAllHistoricalConsumption(
        userId: String
    ): List<HistoricalConsumptionEntity>
}