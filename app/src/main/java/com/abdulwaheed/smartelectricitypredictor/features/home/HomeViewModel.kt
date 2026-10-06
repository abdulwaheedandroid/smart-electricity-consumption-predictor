package com.abdulwaheed.smartelectricitypredictor.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulwaheed.smartelectricitypredictor.domain.calculator.ElectricityConsumptionCalculator
import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ApplianceRepository
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.features.home.state.ApplianceConsumptionEstimate
import com.abdulwaheed.smartelectricitypredictor.features.home.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val applianceRepository: ApplianceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var sessionUid: String? = null
    private var sessionVersion = 0L
    private var observationJob: Job? = null
    private var periodJob: Job? = null
    private var appliances: List<Appliance> = emptyList()
    private var hasLoaded = false

    init { checkSession() }

    fun checkSession() {
        val uid = authRepository.getCurrentUser()?.uid
        if (uid != null && uid == sessionUid) {
            if (hasLoaded) calculateEstimates()
            return
        }
        sessionVersion++
        observationJob?.cancel()
        periodJob?.cancel()
        sessionUid = uid
        appliances = emptyList()
        hasLoaded = false
        _uiState.value = HomeUiState(
            uid = uid, isLoading = uid != null,
            errorMessage = if (uid == null) "Your session has expired. Please sign in again." else null
        )
        if (uid == null) return
        observeAppliances(uid, sessionVersion)
        periodJob = viewModelScope.launch {
            // Refresh an open dashboard across midnight/month boundaries.
            while (true) {
                delay(60_000L)
                checkSession()
            }
        }
    }

    fun retryLoad() {
        checkSession()
        val uid = sessionUid ?: return
        observationJob?.cancel()
        hasLoaded = false
        appliances = emptyList()
        _uiState.value = HomeUiState(uid = uid)
        observeAppliances(uid, sessionVersion)
    }

    private fun isCurrent(uid: String, version: Long): Boolean {
        if (authRepository.getCurrentUser()?.uid != sessionUid) checkSession()
        return uid == sessionUid && version == sessionVersion
    }

    private fun observeAppliances(uid: String, version: Long) {
        observationJob = viewModelScope.launch {
            try {
                applianceRepository.observeAppliances(uid).collect { records ->
                    ensureActive()
                    if (isCurrent(uid, version)) {
                        appliances = records.toList()
                        hasLoaded = true
                        calculateEstimates()
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (isCurrent(uid, version)) {
                    hasLoaded = false
                    appliances = emptyList()
                    _uiState.value = HomeUiState(
                        uid = uid, isLoading = false,
                        errorMessage = "Unable to load your appliances. Please try again."
                    )
                }
            }
        }
    }

    private fun calculateEstimates() {
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH) + 1
        val year = now.get(Calendar.YEAR)
        val calculator = ElectricityConsumptionCalculator
        try {
            _uiState.value = HomeUiState(
                uid = sessionUid, isLoading = false, month = month, year = year,
                daysInMonth = calculator.daysInMonth(month, year),
                totalDailyKwh = calculator.totalDailyKwh(appliances),
                monthlyKwh = calculator.monthlyKwh(appliances, month, year),
                applianceEstimates = appliances.map {
                    ApplianceConsumptionEstimate(it.id, it.name, calculator.dailyKwh(it))
                }
            )
        } catch (exception: IllegalArgumentException) {
            _uiState.value = HomeUiState(
                uid = sessionUid, isLoading = false, month = month, year = year,
                errorMessage = "Unable to estimate consumption. Check appliance power ratings and daily usage hours."
            )
        }
    }
}
