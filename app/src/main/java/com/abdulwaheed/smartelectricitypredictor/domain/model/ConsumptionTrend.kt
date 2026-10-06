package com.abdulwaheed.smartelectricitypredictor.domain.model

data class ConsumptionTrend(
    val month: Int,
    val year: Int,
    val unitsConsumed: Double?,
    val previousUnitsConsumed: Double?,
    /** Signed change in kWh: current minus the immediately preceding month's units. */
    val absoluteChange: Double?,
    /** Null when comparison is unavailable or a nonzero change has a zero baseline. */
    val percentageChange: Double?,
    val direction: TrendDirection
)
