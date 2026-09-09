package com.abdulwaheed.smartelectricitypredictor.features.auth.state

import com.abdulwaheed.smartelectricitypredictor.domain.model.User

enum class AuthStage { CHECKING, SIGNED_OUT, PROFILE_ERROR, NEEDS_PROFILE, READY }

data class AuthUiState(
    val stage: AuthStage = AuthStage.CHECKING,
    // Loading is opt-in for startup checks and active authentication operations.
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

