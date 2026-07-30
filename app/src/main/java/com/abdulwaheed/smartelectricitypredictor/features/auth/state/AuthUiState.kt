package com.abdulwaheed.smartelectricitypredictor.features.auth.state

import com.abdulwaheed.smartelectricitypredictor.domain.model.User

data class AuthUiState(
    // Loading is opt-in for startup checks and active authentication operations.
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

