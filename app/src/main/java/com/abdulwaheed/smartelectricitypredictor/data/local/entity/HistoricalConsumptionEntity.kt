package com.abdulwaheed.smartelectricitypredictor.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "historical_consumption",
    primaryKeys = ["userId", "id"],
    indices = [
        Index(
            value = ["userId", "month", "year"],
            unique = true
        )
    ]
)
data class HistoricalConsumptionEntity(
    val userId: String,
    val id: String,
    val month: Int,
    val year: Int,
    val unitsConsumed: Double,
    val billAmount: Double,
    val createdAt: Long?,
    val updatedAt: Long?,
    val isDeleted: Boolean = false,
    val syncPending: Boolean = false
)