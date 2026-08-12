package com.abdulwaheed.smartelectricitypredictor.features.appliance.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance
import com.abdulwaheed.smartelectricitypredictor.features.appliance.state.ApplianceUiState

@Composable
fun ApplianceScreen(
    state: ApplianceUiState,
    onSearchQueryChanged: (String) -> Unit,
    onAddAppliance: () -> Unit,
    onEditAppliance: (Appliance) -> Unit,
    onDeleteAppliance: (Appliance) -> Unit,
    onNameChanged: (String) -> Unit,
    onPowerWattsChanged: (String) -> Unit,
    onDailyUsageHoursChanged: (String) -> Unit,
    onSaveAppliance: () -> Unit,
    onDismissEditor: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Text("Loading appliances...", modifier = Modifier.padding(top = 12.dp))
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("My Appliances")
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Search appliances") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onAddAppliance, enabled = !state.isSaving && !state.isDeleting, modifier = Modifier.fillMaxWidth()) {
            Text("Add appliance")
        }
        state.errorMessage?.let { message ->
            Text(message)
            Button(onClick = onRetry, enabled = !state.isSaving && !state.isDeleting) { Text("Retry") }
        }
        when {
            state.appliances.isEmpty() -> Text("No appliances added yet. Add an appliance to get started.")
            state.filteredAppliances.isEmpty() -> Text("No appliances match your search.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.filteredAppliances, key = { it.id }) { appliance ->
                    ApplianceItem(appliance, onEditAppliance, onDeleteAppliance)
                }
            }
        }
    }

    if (state.showEditor) {
        ApplianceEditorDialog(
            state = state,
            onNameChanged = onNameChanged,
            onPowerWattsChanged = onPowerWattsChanged,
            onDailyUsageHoursChanged = onDailyUsageHoursChanged,
            onSave = onSaveAppliance,
            onDismiss = onDismissEditor
        )
    }
    state.appliancePendingDeletion?.let { appliance ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Delete appliance?") },
            text = { Text("Delete ${appliance.name}? This cannot be undone.") },
            confirmButton = { TextButton(onClick = onConfirmDelete) { Text("Delete") } },
            dismissButton = { TextButton(onClick = onCancelDelete) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ApplianceItem(
    appliance: Appliance,
    onEdit: (Appliance) -> Unit,
    onDelete: (Appliance) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(appliance) }.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(appliance.name)
        Text("${appliance.powerWatts} W · ${appliance.dailyUsageHours} hours/day")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onEdit(appliance) }) { Text("Edit") }
            TextButton(onClick = { onDelete(appliance) }) { Text("Delete") }
        }
    }
}

@Composable
private fun ApplianceEditorDialog(
    state: ApplianceUiState,
    onNameChanged: (String) -> Unit,
    onPowerWattsChanged: (String) -> Unit,
    onDailyUsageHoursChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.editingApplianceId == null) "Add appliance" else "Edit appliance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.name, onValueChange = onNameChanged, label = { Text("Name") },
                    isError = state.fieldErrors.name != null,
                    supportingText = { state.fieldErrors.name?.let { Text(it) } }, singleLine = true
                )
                OutlinedTextField(
                    value = state.powerWatts, onValueChange = onPowerWattsChanged, label = { Text("Power rating (watts)") },
                    isError = state.fieldErrors.powerWatts != null,
                    supportingText = { state.fieldErrors.powerWatts?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                )
                OutlinedTextField(
                    value = state.dailyUsageHours, onValueChange = onDailyUsageHoursChanged, label = { Text("Daily usage (hours)") },
                    isError = state.fieldErrors.dailyUsageHours != null,
                    supportingText = { state.fieldErrors.dailyUsageHours?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !state.isSaving) {
                if (state.isSaving) CircularProgressIndicator() else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !state.isSaving) { Text("Cancel") } }
    )
}
