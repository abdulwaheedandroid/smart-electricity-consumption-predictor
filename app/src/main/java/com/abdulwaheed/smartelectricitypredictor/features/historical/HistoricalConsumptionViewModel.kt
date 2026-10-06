package com.abdulwaheed.smartelectricitypredictor.features.historical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulwaheed.smartelectricitypredictor.domain.calculator.HistoricalTrendCalculator
import com.abdulwaheed.smartelectricitypredictor.domain.model.HistoricalConsumption
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.domain.repository.HistoricalConsumptionRepository
import com.abdulwaheed.smartelectricitypredictor.features.historical.state.HistoricalConsumptionFieldErrors
import com.abdulwaheed.smartelectricitypredictor.features.historical.state.HistoricalConsumptionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HistoricalConsumptionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val consumptionRepository: HistoricalConsumptionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoricalConsumptionUiState())
    val uiState: StateFlow<HistoricalConsumptionUiState> = _uiState.asStateFlow()
    private var sessionUid: String? = null
    private var sessionVersion = 0L
    private var observationJob: Job? = null
    private var mutationJob: Job? = null
    private var syncJob: Job? = null
    private var syncRequested = false

    init { checkSession() }

    // AuthRepository uses explicit session checks; navigation should also call this
    // when its authentication state changes.
    fun checkSession() {
        val uid = authRepository.getCurrentUser()?.uid
        if (uid == sessionUid && uid != null) return
        sessionVersion++
        observationJob?.cancel()
        mutationJob?.cancel()
        syncJob?.cancel()
        observationJob = null
        mutationJob = null
        syncJob = null
        syncRequested = false
        sessionUid = uid
        _uiState.value = HistoricalConsumptionUiState(
            uid = uid,
            isLoading = uid != null,
            errorMessage = if (uid == null) SESSION_ERROR else null
        )
        if (uid != null) {
            observeRecords(uid, sessionVersion)
            requestSync(uid, sessionVersion)
        }
    }

    private fun currentUid(): String? {
        checkSession()
        return sessionUid
    }

    private fun isCurrent(uid: String, version: Long): Boolean {
        if (authRepository.getCurrentUser()?.uid != sessionUid) checkSession()
        return uid == sessionUid && version == sessionVersion
    }

    private fun observeRecords(uid: String, version: Long) {
        observationJob = viewModelScope.launch {
            try {
                consumptionRepository.observeHistoricalConsumption(uid).collect { records ->
                    ensureActive()
                    if (isCurrent(uid, version)) {
                        _uiState.value = _uiState.value.copy(
                            records = records,
                            trends = HistoricalTrendCalculator.calculateTrends(records),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (isCurrent(uid, version)) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Unable to load saved consumption. Please try again."
                    )
                }
            }
        }
    }

    fun retryLoad() {
        val uid = currentUid() ?: return
        observationJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        observeRecords(uid, sessionVersion)
    }

    fun retrySync() {
        val uid = currentUid() ?: return
        requestSync(uid, sessionVersion)
    }

    private fun requestSync(uid: String, version: Long) {
        // A save during a running sync needs another pass for the new local version.
        syncRequested = true
        if (syncJob?.isActive == true) return
        _uiState.value = _uiState.value.copy(isSyncing = true, syncErrorMessage = null)
        syncJob = viewModelScope.launch {
            while (syncRequested && isCurrent(uid, version)) {
                syncRequested = false
                val result = consumptionRepository.syncHistoricalConsumption(uid)
                ensureActive()
                if (!isCurrent(uid, version)) return@launch
                _uiState.value = _uiState.value.copy(
                    hasSynced = _uiState.value.hasSynced || result.isSuccess,
                    syncErrorMessage = if (result.isFailure)
                        "Unable to sync consumption. Your local changes are saved. Check your connection and retry."
                    else null
                )
            }
            if (isCurrent(uid, version)) {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }

    private fun canMutate(): Boolean = !_uiState.value.isLoading &&
        !_uiState.value.isSaving && !_uiState.value.isDeleting

    fun startAddingConsumption() {
        currentUid() ?: return
        if (!canMutate()) return
        val now = Calendar.getInstance()
        _uiState.value = _uiState.value.copy(
            editingRecordId = null,
            month = (now.get(Calendar.MONTH) + 1).toString(),
            year = now.get(Calendar.YEAR).toString(),
            unitsConsumed = "", billAmount = "",
            fieldErrors = HistoricalConsumptionFieldErrors(),
            showEditor = true, recordPendingDeletion = null, errorMessage = null
        )
    }

    fun startEditingConsumption(record: HistoricalConsumption) {
        currentUid() ?: return
        if (!canMutate()) return
        val current = _uiState.value.records.find { it.id == record.id } ?: return
        _uiState.value = _uiState.value.copy(
            editingRecordId = current.id, month = current.month.toString(),
            year = current.year.toString(), unitsConsumed = current.unitsConsumed.toString(),
            billAmount = current.billAmount.toString(),
            fieldErrors = HistoricalConsumptionFieldErrors(),
            showEditor = true, recordPendingDeletion = null, errorMessage = null
        )
    }

    fun dismissEditor() {
        currentUid() ?: return
        if (!canMutate()) return
        _uiState.value = _uiState.value.copy(showEditor = false, fieldErrors = HistoricalConsumptionFieldErrors())
    }

    fun onMonthChanged(value: String) = updateForm {
        if (editingRecordId == null) copy(month = value, fieldErrors = fieldErrors.copy(month = null)) else this
    }
    fun onYearChanged(value: String) = updateForm {
        if (editingRecordId == null) copy(year = value, fieldErrors = fieldErrors.copy(year = null)) else this
    }
    fun onUnitsConsumedChanged(value: String) = updateForm {
        copy(unitsConsumed = value, fieldErrors = fieldErrors.copy(unitsConsumed = null))
    }
    fun onBillAmountChanged(value: String) = updateForm {
        copy(billAmount = value, fieldErrors = fieldErrors.copy(billAmount = null))
    }

    private fun updateForm(transform: HistoricalConsumptionUiState.() -> HistoricalConsumptionUiState) {
        currentUid() ?: return
        if (canMutate() && _uiState.value.showEditor) {
            _uiState.value = _uiState.value.transform().copy(errorMessage = null)
        }
    }

    fun saveConsumption() {
        val uid = currentUid() ?: return
        val state = _uiState.value
        if (!canMutate() || !state.showEditor) return
        val errors = validate(state)
        if (errors.hasErrors) {
            _uiState.value = state.copy(fieldErrors = errors, errorMessage = null)
            return
        }
        val record = HistoricalConsumption(
            id = state.editingRecordId.orEmpty(), month = state.month.trim().toInt(),
            year = state.year.trim().toInt(), unitsConsumed = state.unitsConsumed.trim().toDouble(),
            billAmount = state.billAmount.trim().toDouble()
        )
        val version = sessionVersion
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        mutationJob = viewModelScope.launch {
            val result = consumptionRepository.saveHistoricalConsumption(uid, record)
            ensureActive()
            if (!isCurrent(uid, version)) return@launch
            _uiState.value = _uiState.value.copy(
                isSaving = false, showEditor = result.isFailure,
                errorMessage = result.exceptionOrNull()?.let { localError(it, "save") }
            )
            if (result.isSuccess) requestSync(uid, version)
        }
    }

    fun requestDeleteConsumption(record: HistoricalConsumption) {
        currentUid() ?: return
        if (!canMutate() || _uiState.value.showEditor) return
        val current = _uiState.value.records.find { it.id == record.id } ?: return
        _uiState.value = _uiState.value.copy(recordPendingDeletion = current, errorMessage = null)
    }

    fun cancelDeleteConsumption() {
        currentUid() ?: return
        if (!canMutate()) return
        _uiState.value = _uiState.value.copy(recordPendingDeletion = null)
    }

    fun confirmDeleteConsumption() {
        val uid = currentUid() ?: return
        if (!canMutate()) return
        val record = _uiState.value.recordPendingDeletion ?: return
        val version = sessionVersion
        _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
        mutationJob = viewModelScope.launch {
            val result = consumptionRepository.deleteHistoricalConsumption(uid, record.id)
            ensureActive()
            if (!isCurrent(uid, version)) return@launch
            _uiState.value = _uiState.value.copy(
                isDeleting = false,
                recordPendingDeletion = if (result.isSuccess) null else record,
                errorMessage = result.exceptionOrNull()?.let { localError(it, "delete") }
            )
            if (result.isSuccess) requestSync(uid, version)
        }
    }

    private fun validate(state: HistoricalConsumptionUiState): HistoricalConsumptionFieldErrors {
        val month = state.month.trim().toIntOrNull()
        val year = state.year.trim().toIntOrNull()
        val units = state.unitsConsumed.trim().toDoubleOrNull()
        val bill = state.billAmount.trim().toDoubleOrNull()
        val now = Calendar.getInstance()
        val future = month != null && month in 1..12 && year != null && year in 1..9999 &&
            (year > now.get(Calendar.YEAR) ||
                (year == now.get(Calendar.YEAR) && month > now.get(Calendar.MONTH) + 1))
        val duplicate = state.editingRecordId == null && state.records.any { it.month == month && it.year == year }
        return HistoricalConsumptionFieldErrors(
            month = when {
                month == null || month !in 1..12 -> "Select a valid month."
                future -> "Consumption cannot be entered for a future month."
                duplicate -> "A record already exists for this month. Edit it instead."
                else -> null
            },
            year = if (year == null || year !in 1..9999) "Enter a valid year (1–9999)." else null,
            unitsConsumed = if (units == null || !units.isFinite() || units < 0)
                "Units consumed must be a valid non-negative number." else null,
            billAmount = if (bill == null || !bill.isFinite() || bill < 0)
                "Bill amount must be a valid non-negative number." else null
        )
    }

    private fun localError(exception: Throwable, action: String): String {
        // Only known repository validation messages may be displayed verbatim.
        val safeMessages = setOf(
            "A record already exists for this month. Edit it instead.",
            "This record no longer exists. Add it again.",
            "Month and year cannot be changed for an existing record.",
            "Consumption cannot be entered for a future month.",
            "Record not found."
        )
        return if (exception is IllegalArgumentException && exception.message in safeMessages)
            exception.message.orEmpty()
        else "Unable to $action consumption on this device. Please try again."
    }

    private companion object {
        const val SESSION_ERROR = "Your session has expired. Please sign in again."
    }
}
