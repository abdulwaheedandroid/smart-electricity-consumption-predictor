package com.abdulwaheed.smartelectricitypredictor.domain.model

data class Appliance(
    val id: String = "",
    val name: String,
    val powerWatts: Int,
    val dailyUsageHours: Double,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
