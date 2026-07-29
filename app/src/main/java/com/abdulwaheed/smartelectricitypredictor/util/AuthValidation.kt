package com.abdulwaheed.smartelectricitypredictor.util

import android.util.Patterns

/**
 * Validation utilities for authentication inputs.
 * Provides reusable validation logic for email and password fields.
 */
object AuthValidation {
    private const val MIN_PASSWORD_LENGTH = 6

    /**
     * Validates email format using Android's Patterns.EMAIL_ADDRESS.
     * Trims whitespace before validation.
     *
     * @param email The email address to validate
     * @return Pair of (isValid, errorMessage)
     */
    fun validateEmail(email: String): Pair<Boolean, String?> {
        val trimmedEmail = email.trim()

        return when {
            trimmedEmail.isEmpty() -> false to "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ->
                false to "Please enter a valid email address"
            else -> true to null
        }
    }

    /**
     * Validates password for login.
     * Requires non-empty password.
     *
     * @param password The password to validate
     * @return Pair of (isValid, errorMessage)
     */
    fun validatePasswordLogin(password: String): Pair<Boolean, String?> {
        return when {
            password.isEmpty() -> false to "Password is required"
            else -> true to null
        }
    }

    /**
     * Validates password for registration.
     * Requires minimum 6 characters (Firebase requirement).
     *
     * @param password The password to validate
     * @return Pair of (isValid, errorMessage)
     */
    fun validatePasswordRegister(password: String): Pair<Boolean, String?> {
        return when {
            password.isEmpty() -> false to "Password is required"
            password.length < MIN_PASSWORD_LENGTH ->
                false to "Password must be at least $MIN_PASSWORD_LENGTH characters"
            else -> true to null
        }
    }

    /**
     * Validates both email and password for login.
     * Returns the first validation error if any.
     *
     * @param email The email to validate
     * @param password The password to validate
     * @return Pair of (isValid, errorMessage)
     */
    fun validateLoginInputs(email: String, password: String): Pair<Boolean, String?> {
        val emailValidation = validateEmail(email)
        if (!emailValidation.first) {
            return emailValidation
        }

        val passwordValidation = validatePasswordLogin(password)
        if (!passwordValidation.first) {
            return passwordValidation
        }

        return true to null
    }

    /**
     * Validates both email and password for registration.
     * Returns the first validation error if any.
     *
     * @param email The email to validate
     * @param password The password to validate
     * @return Pair of (isValid, errorMessage)
     */
    fun validateRegisterInputs(email: String, password: String): Pair<Boolean, String?> {
        val emailValidation = validateEmail(email)
        if (!emailValidation.first) {
            return emailValidation
        }

        val passwordValidation = validatePasswordRegister(password)
        if (!passwordValidation.first) {
            return passwordValidation
        }

        return true to null
    }
}

