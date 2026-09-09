package com.abdulwaheed.smartelectricitypredictor.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ProfileRepository
import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthStage
import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthUiState
import com.abdulwaheed.smartelectricitypredictor.util.FirebaseAuthErrorHandler
import com.abdulwaheed.smartelectricitypredictor.util.FirestoreProfileErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()
    private var sessionJob: Job? = null

    init { checkSession() }

    fun checkSession() {
        sessionJob?.cancel()
        _uiState.value = AuthUiState()
        sessionJob = viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user == null) {
                _uiState.value = AuthUiState(stage = AuthStage.SIGNED_OUT)
                return@launch
            }
            _uiState.value = _uiState.value.copy(user = user)
            val result = profileRepository.getProfile(user.uid)
            ensureActive()
            result.fold(
                onSuccess = { profile ->
                    _uiState.value = AuthUiState(
                        stage = if (profile == null) AuthStage.NEEDS_PROFILE else AuthStage.READY,
                        user = user
                    )
                },
                onFailure = {
                    _uiState.value = AuthUiState(
                        stage = AuthStage.PROFILE_ERROR, user = user,
                        errorMessage = FirestoreProfileErrorHandler.getErrorMessage(it)
                    )
                }
            )
        }
    }

    fun onFirebaseUiSignInStarted(): Boolean {
        if (_uiState.value.stage != AuthStage.SIGNED_OUT || _uiState.value.isLoading) return false
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        return true
    }

    fun handleFirebaseUiSignInResult(success: Boolean, error: Throwable?) {
        if (success) {
            checkSession()
        } else {
            _uiState.value = AuthUiState(
                stage = AuthStage.SIGNED_OUT,
                errorMessage = error?.let(FirebaseAuthErrorHandler::getErrorMessage)
                    ?: "Sign-in was cancelled."
            )
        }
    }

    fun onProfileSaved() {
        _uiState.value = _uiState.value.copy(stage = AuthStage.READY, errorMessage = null)
    }

    fun onProfileDeleted() {
        _uiState.value = _uiState.value.copy(stage = AuthStage.NEEDS_PROFILE, errorMessage = null)
    }

    fun signOut() {
        if (_uiState.value.isLoading) return
        sessionJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true)
        sessionJob = viewModelScope.launch {
            val result = authRepository.signOut()
            ensureActive()
            result.fold(
                onSuccess = { _uiState.value = AuthUiState(stage = AuthStage.SIGNED_OUT) },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = FirebaseAuthErrorHandler.getErrorMessage(it)
                    )
                }
            )
        }
    }
}
