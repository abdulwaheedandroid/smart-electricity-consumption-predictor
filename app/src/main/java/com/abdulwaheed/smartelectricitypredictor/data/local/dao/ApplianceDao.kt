package com.abdulwaheed.smartelectricitypredictor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abdulwaheed.smartelectricitypredictor.data.local.entity.ApplianceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplianceDao {

    @Query(
        """
        SELECT * FROM appliances
        WHERE userId = :userId
        AND isDeleted = 0
        ORDER BY createdAt DESC
        """
    )
    fun observeAppliances(userId: String): Flow<List<ApplianceEntity>>

    @Query(
        """
        SELECT * FROM appliances
        WHERE userId = :userId
        AND id = :applianceId
        LIMIT 1
        """
    )
    suspend fun getAppliance(
        userId: String,
        applianceId: String
    ): ApplianceEntity?

    @Upsert
    suspend fun upsertAppliance(appliance: ApplianceEntity)

    @Upsert
    suspend fun upsertAppliances(appliances: List<ApplianceEntity>)

    @Query(
        """
    UPDATE appliances
    SET isDeleted = 1,
        syncPending = 1,
        updatedAt = :updatedAt
    WHERE userId = :userId
    AND id = :applianceId
    """
    )
    suspend fun markDeleted(
        userId: String,
        applianceId: String,
        updatedAt: Long
    )

    @Query(
        """
    UPDATE appliances
    SET syncPending = :syncPending
    WHERE userId = :userId
    AND id = :applianceId
    """
    )
    suspend fun updateSyncPending(
        userId: String,
        applianceId: String,
        syncPending: Boolean
    )

    @Query(
        """
    SELECT * FROM appliances
    WHERE userId = :userId
    AND syncPending = 1
    """
    )
    suspend fun getPendingSyncAppliances(
        userId: String
    ): List<ApplianceEntity>

    @Query(
        """
    SELECT * FROM appliances
    WHERE userId = :userId
    """
    )
    suspend fun getAllAppliances(
        userId: String
    ): List<ApplianceEntity>
}