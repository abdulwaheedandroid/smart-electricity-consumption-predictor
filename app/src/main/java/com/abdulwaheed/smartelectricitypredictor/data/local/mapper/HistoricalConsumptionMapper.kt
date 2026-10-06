package com.abdulwaheed.smartelectricitypredictor.data.local.mapper

import com.abdulwaheed.smartelectricitypredictor.data.local.entity.HistoricalConsumptionEntity
import com.abdulwaheed.smartelectricitypredictor.domain.model.HistoricalConsumption

fun HistoricalConsumptionEntity.toDomain(): HistoricalConsumption {
    return HistoricalConsumption(
        id = id,
        month = month,
        year = year,
        unitsConsumed = unitsConsumed,
        billAmount = billAmount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun HistoricalConsumption.toEntity(
    userId: String,
    isDeleted: Boolean = false,
    syncPending: Boolean = false
): HistoricalConsumptionEntity {
    return HistoricalConsumptionEntity(
        userId = userId,
        id = id,
        month = month,
        year = year,
        unitsConsumed = unitsConsumed,
        billAmount = billAmount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        syncPending = syncPending
    )
}