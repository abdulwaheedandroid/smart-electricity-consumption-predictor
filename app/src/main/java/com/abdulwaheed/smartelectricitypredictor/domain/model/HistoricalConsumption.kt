package com.abdulwaheed.smartelectricitypredictor.domain.model

data class HistoricalConsumption(
    val id: String = "",
    val month: Int,
    val year: Int,
    val unitsConsumed: Double,
    val billAmount: Double,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
