package com.abdulwaheed.smartelectricitypredictor.features.profile.state

data class ProfileFieldErrors(
    val fullName: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val cellNumber: String? = null
) {
    val hasErrors: Boolean
        get() = fullName != null || age != null || gender != null || cellNumber != null
}

data class ProfileUiState(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val age: String = "",
    val gender: String = "",
    val cellNumber: String = "",
    val fieldErrors: ProfileFieldErrors = ProfileFieldErrors(),
    val profileExists: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val errorMessage: String? = null
)
