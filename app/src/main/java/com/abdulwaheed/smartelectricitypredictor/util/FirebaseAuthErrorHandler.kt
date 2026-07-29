package com.abdulwaheed.smartelectricitypredictor.util

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Maps Firebase Authentication exceptions to user-friendly error messages.
 * Keeps Firebase-specific error handling outside composables.
 */
object FirebaseAuthErrorHandler {
    fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                when {
                    exception.message?.contains("email address is malformed", ignoreCase = true) == true ->
                        "Invalid email address format"
                    exception.message?.contains("password is invalid", ignoreCase = true) == true ->
                        "Incorrect password"
                    else -> "Invalid credentials. Please check your email and password."
                }
            }
            is FirebaseAuthInvalidUserException -> {
                when {
                    exception.message?.contains("no user record", ignoreCase = true) == true ->
                        "No account found with this email address"
                    exception.message?.contains("user has been disabled", ignoreCase = true) == true ->
                        "This account has been disabled"
                    else -> "User not found or account error"
                }
            }
            is FirebaseAuthUserCollisionException -> {
                "Email address is already in use. Please try signing in or use a different email."
            }
            is FirebaseAuthWeakPasswordException -> {
                "Password is too weak. Please use at least 6 characters."
            }
            else -> {
                // Check for common error messages in generic exceptions
                when {
                    exception.message?.contains("too many requests", ignoreCase = true) == true ->
                        "Too many failed attempts. Please try again later."
                    exception.message?.contains("network error", ignoreCase = true) == true ->
                        "Network error. Please check your internet connection."
                    exception.message?.contains("error", ignoreCase = true) == true ->
                        "Authentication failed. Please try again."
                    else -> "An unexpected error occurred. Please try again."
                }
            }
        }
    }
}


