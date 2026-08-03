package com.abdulwaheed.smartelectricitypredictor.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulwaheed.smartelectricitypredictor.di.ServiceLocator
import com.abdulwaheed.smartelectricitypredictor.domain.model.User
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ProfileRepository
import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthUiState
import com.abdulwaheed.smartelectricitypredictor.navigation.NavDest
import com.abdulwaheed.smartelectricitypredictor.util.AuthValidation
import com.abdulwaheed.smartelectricitypredictor.util.FirebaseAuthErrorHandler
import com.abdulwaheed.smartelectricitypredictor.util.FirestoreProfileErrorHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = ServiceLocator.authRepository,
    private val profileRepository: ProfileRepository = ServiceLocator.profileRepository
) : ViewModel() {
    // Startup and authentication operations explicitly opt into loading as needed.
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    // make nav events replay the last emission so that a collector started slightly after an emit
    // will still receive the navigation request (prevents lost events during startup race)
    private val _navEvents = MutableSharedFlow<String>(replay = 1)
    val navEvents = _navEvents.asSharedFlow()

    fun checkAuthAndNavigate() {
        Log.d("AuthViewModel", "checkAuthAndNavigate: starting")
        // ensure we show loading while checking
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    Log.d("AuthViewModel", "checkAuthAndNavigate: user found=${user.email}")
                    routeAuthenticatedUser(user)
                } else {
                    Log.d("AuthViewModel", "checkAuthAndNavigate: no user")
                    _uiState.value = AuthUiState(isLoading = false, user = null)
                    _navEvents.emit(NavDest.Login.route)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "checkAuthAndNavigate failed", e)
                _uiState.value = AuthUiState(
                    isLoading = false,
                    errorMessage = "Unable to check your session. Please try again."
                )
                _navEvents.emit(NavDest.Login.route)
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        // Validate inputs before sending to Firebase
        val (isValid, validationError) = AuthValidation.validateLoginInputs(email, password)
        if (!isValid) {
            _uiState.value = AuthUiState(isLoading = false, errorMessage = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val res = authRepository.signInWithEmail(email.trim(), password)
            res.fold(onSuccess = { user ->
                routeAuthenticatedUser(user)
            }, onFailure = { e ->
                val userFriendlyError = FirebaseAuthErrorHandler.getErrorMessage(e)
                _uiState.value = AuthUiState(isLoading = false, errorMessage = userFriendlyError)
            })
        }
    }

    fun onGoogleSignInStarted() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
    }

    fun handleGoogleSignInResult(success: Boolean, error: Throwable?) {
        if (success) {
            // FirebaseUI has already updated FirebaseAuth.currentUser.
            checkAuthAndNavigate()
            return
        }

        val message = if (error == null) {
            "Google sign-in was cancelled."
        } else {
            FirebaseAuthErrorHandler.getErrorMessage(error)
        }
        _uiState.value = AuthUiState(isLoading = false, errorMessage = message)
    }

    fun signUpWithEmail(email: String, password: String) {
        // Validate inputs before sending to Firebase
        val (isValid, validationError) = AuthValidation.validateRegisterInputs(email, password)
        if (!isValid) {
            _uiState.value = AuthUiState(isLoading = false, errorMessage = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val res = authRepository.signUpWithEmail(email.trim(), password)
            res.fold(onSuccess = { user ->
                routeAuthenticatedUser(user)
            }, onFailure = { e ->
                val userFriendlyError = FirebaseAuthErrorHandler.getErrorMessage(e)
                _uiState.value = AuthUiState(isLoading = false, errorMessage = userFriendlyError)
            })
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val res = authRepository.signOut()
            res.fold(onSuccess = {
                _uiState.value = AuthUiState(isLoading = false, user = null)
                _navEvents.emit(NavDest.Login.route)
            }, onFailure = { e ->
                val userFriendlyError = FirebaseAuthErrorHandler.getErrorMessage(e)
                _uiState.value = AuthUiState(isLoading = false, errorMessage = userFriendlyError)
            })
        }
    }

    private suspend fun routeAuthenticatedUser(user: User) {
        _uiState.value = AuthUiState(isLoading = true, user = user)
        profileRepository.getProfile(user.uid).fold(
            onSuccess = { profile ->
                _uiState.value = AuthUiState(isLoading = false, user = user)
                _navEvents.emit(
                    if (profile == null) NavDest.ProfileSetup.route else NavDest.Home.route
                )
            },
            onFailure = { exception ->
                Log.e("AuthViewModel", "Profile check failed", exception)
                _uiState.value = AuthUiState(
                    isLoading = false,
                    user = user,
                    errorMessage = FirestoreProfileErrorHandler.getErrorMessage(exception)
                )
            }
        )
    }
}

