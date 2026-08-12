package com.abdulwaheed.smartelectricitypredictor.features.appliance.state

import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance

data class ApplianceFieldErrors(
    val name: String? = null,
    val powerWatts: String? = null,
    val dailyUsageHours: String? = null
) {
    val hasErrors: Boolean
        get() = name != null || powerWatts != null || dailyUsageHours != null
}

data class ApplianceUiState(
    val appliances: List<Appliance> = emptyList(),
    val searchQuery: String = "",
    val editingApplianceId: String? = null,
    val name: String = "",
    val powerWatts: String = "",
    val dailyUsageHours: String = "",
    val fieldErrors: ApplianceFieldErrors = ApplianceFieldErrors(),
    val appliancePendingDeletion: Appliance? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showEditor: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredAppliances: List<Appliance>
        get() = if (searchQuery.isBlank()) appliances else appliances.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true)
        }
}
