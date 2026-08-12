package com.abdulwaheed.smartelectricitypredictor.features.appliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ApplianceRepository
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.features.appliance.state.ApplianceFieldErrors
import com.abdulwaheed.smartelectricitypredictor.features.appliance.state.ApplianceUiState
import com.abdulwaheed.smartelectricitypredictor.util.ApplianceValidation
import com.abdulwaheed.smartelectricitypredictor.util.FirestoreApplianceErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplianceViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val applianceRepository: ApplianceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApplianceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAppliances()
    }

    fun loadAppliances() {
        val uid = authRepository.getCurrentUser()?.uid
        if (uid == null) {
            _uiState.value = ApplianceUiState(
                isLoading = false,
                errorMessage = "Your session has expired. Please sign in again."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            applianceRepository.getAppliances(uid).fold(
                onSuccess = { appliances ->
                    _uiState.value = _uiState.value.copy(
                        appliances = appliances,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = FirestoreApplianceErrorHandler.getErrorMessage(exception)
                    )
                }
            )
        }
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.value = _uiState.value.copy(searchQuery = value)
    }

    fun startAddingAppliance() {
        if (_uiState.value.isSaving || _uiState.value.isDeleting) return
        _uiState.value = _uiState.value.copy(
            editingApplianceId = null,
            name = "",
            powerWatts = "",
            dailyUsageHours = "",
            fieldErrors = ApplianceFieldErrors(),
            showEditor = true,
            errorMessage = null
        )
    }

    fun startEditingAppliance(appliance: Appliance) {
        if (_uiState.value.isSaving || _uiState.value.isDeleting) return
        _uiState.value = _uiState.value.copy(
            editingApplianceId = appliance.id,
            name = appliance.name,
            powerWatts = appliance.powerWatts.toString(),
            dailyUsageHours = appliance.dailyUsageHours.toString(),
            fieldErrors = ApplianceFieldErrors(),
            showEditor = true,
            errorMessage = null
        )
    }

    fun dismissEditor() {
        if (_uiState.value.isSaving) return
        _uiState.value = _uiState.value.copy(showEditor = false, fieldErrors = ApplianceFieldErrors())
    }

    fun onNameChanged(value: String) = updateForm { copy(name = value, fieldErrors = fieldErrors.copy(name = null)) }
    fun onPowerWattsChanged(value: String) = updateForm { copy(powerWatts = value, fieldErrors = fieldErrors.copy(powerWatts = null)) }
    fun onDailyUsageHoursChanged(value: String) = updateForm { copy(dailyUsageHours = value, fieldErrors = fieldErrors.copy(dailyUsageHours = null)) }

    fun saveAppliance() {
        val state = _uiState.value
        if (state.isSaving || state.isDeleting) return
        val uid = authRepository.getCurrentUser()?.uid
        if (uid == null) {
            _uiState.value = state.copy(errorMessage = "Your session has expired. Please sign in again.")
            return
        }
        val errors = ApplianceValidation.validate(state.name, state.powerWatts, state.dailyUsageHours)
        if (errors.hasErrors) {
            _uiState.value = state.copy(fieldErrors = errors)
            return
        }
        val appliance = Appliance(
            id = state.editingApplianceId.orEmpty(),
            name = state.name.trim(),
            powerWatts = state.powerWatts.trim().toInt(),
            dailyUsageHours = state.dailyUsageHours.trim().toDouble()
        )
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            applianceRepository.saveAppliance(uid, appliance).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, showEditor = false)
                    loadAppliances()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = FirestoreApplianceErrorHandler.getErrorMessage(exception)
                    )
                }
            )
        }
    }

    fun requestDeleteAppliance(appliance: Appliance) {
        if (_uiState.value.isSaving || _uiState.value.isDeleting) return
        _uiState.value = _uiState.value.copy(appliancePendingDeletion = appliance, errorMessage = null)
    }

    fun cancelDeleteAppliance() {
        if (_uiState.value.isDeleting) return
        _uiState.value = _uiState.value.copy(appliancePendingDeletion = null)
    }

    fun confirmDeleteAppliance() {
        val state = _uiState.value
        val appliance = state.appliancePendingDeletion ?: return
        val uid = authRepository.getCurrentUser()?.uid
        if (uid == null) {
            _uiState.value = state.copy(
                appliancePendingDeletion = null,
                errorMessage = "Your session has expired. Please sign in again."
            )
            return
        }
        _uiState.value = state.copy(isDeleting = true, appliancePendingDeletion = null, errorMessage = null)
        viewModelScope.launch {
            applianceRepository.deleteAppliance(uid, appliance.id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    loadAppliances()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = FirestoreApplianceErrorHandler.getErrorMessage(exception)
                    )
                }
            )
        }
    }

    private fun updateForm(transform: ApplianceUiState.() -> ApplianceUiState) {
        val state = _uiState.value
        if (!state.isSaving) _uiState.value = state.transform().copy(errorMessage = null)
    }
}
