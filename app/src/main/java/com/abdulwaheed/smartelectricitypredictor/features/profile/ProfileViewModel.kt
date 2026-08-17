package com.abdulwaheed.smartelectricitypredictor.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulwaheed.smartelectricitypredictor.domain.model.UserProfile
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ProfileRepository
import com.abdulwaheed.smartelectricitypredictor.features.profile.state.ProfileUiState
import com.abdulwaheed.smartelectricitypredictor.util.FirestoreProfileErrorHandler
import com.abdulwaheed.smartelectricitypredictor.util.ProfileValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = ProfileUiState(
                isLoading = false,
                errorMessage = "Your session has expired. Please sign in again."
            )
            return
        }
        val email = currentUser.email
        if (email.isNullOrBlank()) {
            _uiState.value = ProfileUiState(
                uid = currentUser.uid,
                isLoading = false,
                errorMessage = "Your authenticated account does not provide an email address."
            )
            return
        }

        _uiState.value = ProfileUiState(uid = currentUser.uid, email = email, isLoading = true)
        viewModelScope.launch {
            profileRepository.getProfile(currentUser.uid).fold(
                onSuccess = { profile ->
                    _uiState.value = _uiState.value.copy(
                        fullName = profile?.fullName.orEmpty(),
                        age = profile?.age?.toString().orEmpty(),
                        gender = profile?.gender.orEmpty(),
                        cellNumber = profile?.cellNumber.orEmpty(),
                        profileExists = profile != null,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = FirestoreProfileErrorHandler.getErrorMessage(exception)
                    )
                }
            )
        }
    }

    fun onFullNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            fullName = value,
            fieldErrors = _uiState.value.fieldErrors.copy(fullName = null),
            errorMessage = null
        )
    }

    fun onAgeChanged(value: String) {
        if (!value.all(Char::isDigit)) return
        _uiState.value = _uiState.value.copy(
            age = value,
            fieldErrors = _uiState.value.fieldErrors.copy(age = null),
            errorMessage = null
        )
    }

    fun onGenderChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            gender = value,
            fieldErrors = _uiState.value.fieldErrors.copy(gender = null),
            errorMessage = null
        )
    }

    fun onCellNumberChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            cellNumber = value,
            fieldErrors = _uiState.value.fieldErrors.copy(cellNumber = null),
            errorMessage = null
        )
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || state.isDeleting) return
        if (state.uid.isBlank() || state.email.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Your account information is unavailable. Please sign in again."
            )
            return
        }

        val errors = ProfileValidation.validate(
            fullName = state.fullName,
            age = state.age,
            gender = state.gender,
            cellNumber = state.cellNumber
        )
        if (errors.hasErrors) {
            _uiState.value = state.copy(fieldErrors = errors, errorMessage = null)
            return
        }

        val profile = UserProfile(
            uid = state.uid,
            fullName = state.fullName.trim(),
            email = state.email,
            age = state.age.trim().toInt(),
            gender = state.gender.trim(),
            cellNumber = state.cellNumber.trim()
        )
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            profileRepository.saveProfile(profile).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        profileExists = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = FirestoreProfileErrorHandler.getErrorMessage(exception)
                    )
                }
            )
        }
    }

    fun requestDeleteProfile() {
        val state = _uiState.value
        if (!state.profileExists || state.isSaving || state.isDeleting) return
        _uiState.value = state.copy(showDeleteConfirmation = true, errorMessage = null)
    }

    fun cancelDeleteProfile() {
        if (_uiState.value.isDeleting) return
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
    }

    fun confirmDeleteProfile() {
        val state = _uiState.value
        if (!state.profileExists || state.isSaving || state.isDeleting || state.uid.isBlank()) return
        _uiState.value = state.copy(
            isDeleting = true,
            showDeleteConfirmation = false,
            errorMessage = null
        )
        viewModelScope.launch {
            profileRepository.deleteProfile(state.uid).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        profileExists = false,
                        fullName = "",
                        age = "",
                        gender = "",
                        cellNumber = ""
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = FirestoreProfileErrorHandler.getErrorMessage(exception)
                    )
                }
            )
        }
    }
}
