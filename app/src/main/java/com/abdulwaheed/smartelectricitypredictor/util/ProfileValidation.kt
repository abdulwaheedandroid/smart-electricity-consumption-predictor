package com.abdulwaheed.smartelectricitypredictor.util

import com.abdulwaheed.smartelectricitypredictor.features.profile.state.ProfileFieldErrors

object ProfileValidation {
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 80
    private const val MAX_GENDER_LENGTH = 30
    private const val MIN_PHONE_DIGITS = 7
    private const val MAX_PHONE_DIGITS = 15
    private const val MAX_AGE = 120

    fun validate(
        fullName: String,
        age: String,
        gender: String,
        cellNumber: String
    ): ProfileFieldErrors {
        val trimmedName = fullName.trim()
        val trimmedGender = gender.trim()
        val trimmedPhone = cellNumber.trim()
        val phoneDigits = trimmedPhone.count(Char::isDigit)
        val parsedAge = age.trim().toIntOrNull()

        return ProfileFieldErrors(
            fullName = when {
                trimmedName.isEmpty() -> "Full name is required"
                trimmedName.length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH ->
                    "Full name must be $MIN_NAME_LENGTH-$MAX_NAME_LENGTH characters"
                else -> null
            },
            age = when {
                age.isBlank() -> "Age is required"
                parsedAge == null -> "Age must be a whole number"
                parsedAge !in 1..MAX_AGE -> "Age must be between 1 and $MAX_AGE"
                else -> null
            },
            gender = when {
                trimmedGender.isEmpty() -> "Gender is required"
                trimmedGender.length > MAX_GENDER_LENGTH ->
                    "Gender must not exceed $MAX_GENDER_LENGTH characters"
                else -> null
            },
            cellNumber = when {
                trimmedPhone.isEmpty() -> "Cell number is required"
                !trimmedPhone.matches(Regex("^\\+?[0-9 ()-]+$")) -> "Enter a valid cell number"
                phoneDigits !in MIN_PHONE_DIGITS..MAX_PHONE_DIGITS ->
                    "Cell number must contain $MIN_PHONE_DIGITS-$MAX_PHONE_DIGITS digits"
                else -> null
            }
        )
    }
}
