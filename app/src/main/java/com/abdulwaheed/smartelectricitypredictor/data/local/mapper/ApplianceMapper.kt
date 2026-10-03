package com.abdulwaheed.smartelectricitypredictor.data.local.mapper

import com.abdulwaheed.smartelectricitypredictor.data.local.entity.ApplianceEntity
import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance

fun ApplianceEntity.toDomain(): Appliance {
    return Appliance(
        id = id,
        name = name,
        powerWatts = powerWatts,
        dailyUsageHours = dailyUsageHours,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Appliance.toEntity(
    userId: String,
    isDeleted: Boolean = false,
    syncPending: Boolean = false
): ApplianceEntity {
    return ApplianceEntity(
        userId = userId,
        id = id,
        name = name,
        powerWatts = powerWatts,
        dailyUsageHours = dailyUsageHours,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        syncPending = syncPending
    )
}