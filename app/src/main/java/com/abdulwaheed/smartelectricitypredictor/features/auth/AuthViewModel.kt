package com.abdulwaheed.smartelectricitypredictor.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthUiState
import com.abdulwaheed.smartelectricitypredictor.navigation.NavDest
import com.abdulwaheed.smartelectricitypredictor.di.ServiceLocator
import com.abdulwaheed.smartelectricitypredictor.util.FirebaseAuthErrorHandler
import com.abdulwaheed.smartelectricitypredictor.util.AuthValidation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import android.util.Log

class AuthViewModel : ViewModel() {
    private val authRepository: AuthRepository = ServiceLocator.authRepository
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
        try {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                Log.d("AuthViewModel", "checkAuthAndNavigate: user found=${user.email}")
                _uiState.value = AuthUiState(isLoading = false, user = user)
                viewModelScope.launch { _navEvents.emit(NavDest.Home.route) }
            } else {
                Log.d("AuthViewModel", "checkAuthAndNavigate: no user")
                _uiState.value = AuthUiState(isLoading = false, user = null)
                viewModelScope.launch { _navEvents.emit(NavDest.Login.route) }
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "checkAuthAndNavigate failed", e)
            // On failure, don't remain stuck in loading; treat as unauthenticated and allow navigation
            _uiState.value = AuthUiState(isLoading = false, errorMessage = e.localizedMessage)
            viewModelScope.launch { _navEvents.emit(NavDest.Login.route) }
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
                _uiState.value = AuthUiState(isLoading = false, user = user)
                _navEvents.emit(NavDest.Home.route)
            }, onFailure = { e ->
                val userFriendlyError = FirebaseAuthErrorHandler.getErrorMessage(e)
                _uiState.value = AuthUiState(isLoading = false, errorMessage = userFriendlyError)
            })
        }
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
                _uiState.value = AuthUiState(isLoading = false, user = user)
                _navEvents.emit(NavDest.Home.route)
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
}

