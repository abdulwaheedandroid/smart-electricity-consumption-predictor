package com.abdulwaheed.smartelectricitypredictor.data.local.entity

import androidx.room.Entity


@Entity(
    tableName = "appliances",
    primaryKeys = ["userId","id"]
)
data class ApplianceEntity(
    val userId: String,
    val id: String,
    val name: String,
    val powerWatts: Int,
    val dailyUsageHours: Double,
    val createdAt: Long?,
    val updatedAt: Long?,
    val isDeleted: Boolean = false,
    val syncPending: Boolean = false
)