package com.abdulwaheed.smartelectricitypredictor.domain.calculator

import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance

/** Estimates from the supplied appliance settings, independently of actual bill history. */
object ElectricityConsumptionCalculator {
    fun dailyKwh(appliance: Appliance): Double {
        require(appliance.powerWatts > 0) { "Power rating must be positive." }
        require(appliance.dailyUsageHours.isFinite() && appliance.dailyUsageHours in 0.0..24.0) {
            "Daily usage must be a finite number between 0 and 24 hours."
        }
        return appliance.powerWatts.toDouble() * appliance.dailyUsageHours / 1_000.0
    }

    fun totalDailyKwh(appliances: List<Appliance>): Double = appliances.sumOf(::dailyKwh)

    /** Assumes the supplied daily settings apply on every day of the selected month. */
    fun monthlyKwh(appliances: List<Appliance>, month: Int, year: Int): Double {
        val days = daysInMonth(month, year)
        return totalDailyKwh(appliances) * days
    }

    /** Gregorian month length, without Android APIs or device clock dependencies. */
    fun daysInMonth(month: Int, year: Int): Int {
        require(month in 1..12) { "Month must be between 1 and 12." }
        require(year in 1..9999) { "Year must be between 1 and 9999." }
        return when (month) {
            2 -> if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }
}
