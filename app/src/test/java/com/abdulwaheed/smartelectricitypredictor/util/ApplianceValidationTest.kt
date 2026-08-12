package com.abdulwaheed.smartelectricitypredictor.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplianceValidationTest {
    @Test
    fun validAppliance_hasNoErrors() {
        val result = ApplianceValidation.validate("Ceiling Fan", "75", "8.5")

        assertFalse(result.hasErrors)
    }

    @Test
    fun emptyFields_haveRequiredErrors() {
        val result = ApplianceValidation.validate("", "", "")

        assertTrue(result.hasErrors)
        assertNotNull(result.name)
        assertNotNull(result.powerWatts)
        assertNotNull(result.dailyUsageHours)
    }

    @Test
    fun powerWatts_mustBePositiveWholeNumberWithinLimit() {
        assertNotNull(ApplianceValidation.validate("Fan", "0", "1").powerWatts)
        assertNotNull(ApplianceValidation.validate("Fan", "2.5", "1").powerWatts)
        assertNotNull(ApplianceValidation.validate("Fan", "100001", "1").powerWatts)
        assertNull(ApplianceValidation.validate("Fan", "100000", "1").powerWatts)
    }

    @Test
    fun dailyUsageHours_mustBeBetweenZeroAndTwentyFour() {
        assertNotNull(ApplianceValidation.validate("Fan", "75", "-0.5").dailyUsageHours)
        assertNotNull(ApplianceValidation.validate("Fan", "75", "24.1").dailyUsageHours)
        assertNull(ApplianceValidation.validate("Fan", "75", "0").dailyUsageHours)
        assertNull(ApplianceValidation.validate("Fan", "75", "24").dailyUsageHours)
    }
}
