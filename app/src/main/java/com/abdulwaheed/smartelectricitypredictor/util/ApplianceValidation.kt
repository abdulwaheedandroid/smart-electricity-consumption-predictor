package com.abdulwaheed.smartelectricitypredictor.util

import com.abdulwaheed.smartelectricitypredictor.features.appliance.state.ApplianceFieldErrors

object ApplianceValidation {
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 80
    private const val MAX_POWER_WATTS = 100_000

    fun validate(name: String, powerWatts: String, dailyUsageHours: String): ApplianceFieldErrors {
        val parsedPowerWatts = powerWatts.trim().toIntOrNull()
        val parsedDailyUsageHours = dailyUsageHours.trim().toDoubleOrNull()

        return ApplianceFieldErrors(
            name = when {
                name.trim().isEmpty() -> "Appliance name is required"
                name.trim().length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH ->
                    "Appliance name must be $MIN_NAME_LENGTH-$MAX_NAME_LENGTH characters"
                else -> null
            },
            powerWatts = when {
                powerWatts.isBlank() -> "Power rating is required"
                parsedPowerWatts == null -> "Power rating must be a whole number"
                parsedPowerWatts !in 1..MAX_POWER_WATTS ->
                    "Power rating must be between 1 and $MAX_POWER_WATTS watts"
                else -> null
            },
            dailyUsageHours = when {
                dailyUsageHours.isBlank() -> "Daily usage is required"
                parsedDailyUsageHours == null || !parsedDailyUsageHours.isFinite() ->
                    "Daily usage must be a number"
                parsedDailyUsageHours < 0.0 || parsedDailyUsageHours > 24.0 ->
                    "Daily usage must be between 0 and 24 hours"
                else -> null
            }
        )
    }
}
