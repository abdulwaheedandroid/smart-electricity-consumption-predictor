package com.abdulwaheed.smartelectricitypredictor.features.auth.state

import com.abdulwaheed.smartelectricitypredictor.domain.model.User

data class AuthUiState(
    // Start in loading state by default so the splash shows progress while startup checks run
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null
)

