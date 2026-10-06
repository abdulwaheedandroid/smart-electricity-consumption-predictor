package com.abdulwaheed.smartelectricitypredictor.features.home.state

data class ApplianceConsumptionEstimate(
    val id: String,
    val name: String,
    val dailyKwh: Double
)

data class HomeUiState(
    val uid: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val month: Int = 1,
    val year: Int = 1,
    val daysInMonth: Int = 31,
    val totalDailyKwh: Double = 0.0,
    val monthlyKwh: Double = 0.0,
    val applianceEstimates: List<ApplianceConsumptionEstimate> = emptyList()
)
