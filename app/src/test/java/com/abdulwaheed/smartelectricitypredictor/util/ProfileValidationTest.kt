package com.abdulwaheed.smartelectricitypredictor.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidationTest {
    @Test
    fun validProfile_hasNoErrors() {
        val result = ProfileValidation.validate(
            fullName = "Abdul Waheed",
            age = "25",
            gender = "Male",
            cellNumber = "+92 300 1234567"
        )

        assertFalse(result.hasErrors)
    }

    @Test
    fun emptyFields_haveRequiredErrors() {
        val result = ProfileValidation.validate("", "", "", "")

        assertTrue(result.hasErrors)
        assertNotNull(result.fullName)
        assertNotNull(result.age)
        assertNotNull(result.gender)
        assertNotNull(result.cellNumber)
    }

    @Test
    fun ageAtBoundaries_isValid() {
        val minimum = ProfileValidation.validate("Valid Name", "1", "Other", "1234567")
        val maximum = ProfileValidation.validate("Valid Name", "120", "Other", "1234567")

        assertNull(minimum.age)
        assertNull(maximum.age)
    }

    @Test
    fun ageOutsideRangeOrNonNumeric_isInvalid() {
        val zero = ProfileValidation.validate("Valid Name", "0", "Other", "1234567")
        val aboveMaximum = ProfileValidation.validate("Valid Name", "121", "Other", "1234567")
        val decimal = ProfileValidation.validate("Valid Name", "2.5", "Other", "1234567")

        assertNotNull(zero.age)
        assertNotNull(aboveMaximum.age)
        assertNotNull(decimal.age)
    }

    @Test
    fun invalidCellNumber_hasError() {
        val tooShort = ProfileValidation.validate("Valid Name", "25", "Other", "123")
        val invalidCharacters = ProfileValidation.validate("Valid Name", "25", "Other", "abcdefghi")

        assertNotNull(tooShort.cellNumber)
        assertNotNull(invalidCharacters.cellNumber)
    }
}
