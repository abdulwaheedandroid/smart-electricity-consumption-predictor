package com.abdulwaheed.smartelectricitypredictor.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthUiState
import com.abdulwaheed.smartelectricitypredictor.navigation.NavDest
import com.abdulwaheed.smartelectricitypredictor.util.FirebaseAuthErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
                    _uiState.value = AuthUiState(isLoading = false, user = user)
                    _navEvents.emit(NavDest.Profile.route)
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

    fun onFirebaseUiSignInStarted() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
    }

    fun handleFirebaseUiSignInResult(success: Boolean, error: Throwable?) {
        if (success) {
            // FirebaseUI has already updated FirebaseAuth.currentUser.
            checkAuthAndNavigate()
            return
        }

        val message = if (error == null) {
            "Sign-in was cancelled."
        } else {
            FirebaseAuthErrorHandler.getErrorMessage(error)
        }
        _uiState.value = AuthUiState(isLoading = false, errorMessage = message)
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

