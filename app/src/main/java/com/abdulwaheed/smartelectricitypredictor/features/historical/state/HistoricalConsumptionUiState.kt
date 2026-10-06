package com.abdulwaheed.smartelectricitypredictor.features.historical.state

import com.abdulwaheed.smartelectricitypredictor.domain.model.HistoricalConsumption
import com.abdulwaheed.smartelectricitypredictor.domain.model.ConsumptionTrend

data class HistoricalConsumptionFieldErrors(
    val month: String? = null,
    val year: String? = null,
    val unitsConsumed: String? = null,
    val billAmount: String? = null
) {
    val hasErrors: Boolean
        get() = month != null || year != null || unitsConsumed != null || billAmount != null
}

data class HistoricalConsumptionUiState(
    val uid: String? = null,
    val records: List<HistoricalConsumption> = emptyList(),
    val trends: List<ConsumptionTrend> = emptyList(),
    val editingRecordId: String? = null,
    val month: String = "",
    val year: String = "",
    val unitsConsumed: String = "",
    val billAmount: String = "",
    val fieldErrors: HistoricalConsumptionFieldErrors = HistoricalConsumptionFieldErrors(),
    val recordPendingDeletion: HistoricalConsumption? = null,
    val showEditor: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val isSyncing: Boolean = false,
    val hasSynced: Boolean = false,
    val syncErrorMessage: String? = null
)
