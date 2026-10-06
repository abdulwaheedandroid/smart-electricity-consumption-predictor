package com.abdulwaheed.smartelectricitypredictor.features.historical.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdulwaheed.smartelectricitypredictor.domain.model.HistoricalConsumption
import com.abdulwaheed.smartelectricitypredictor.domain.model.ConsumptionTrend
import com.abdulwaheed.smartelectricitypredictor.domain.model.TrendDirection
import com.abdulwaheed.smartelectricitypredictor.features.historical.state.HistoricalConsumptionUiState
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun HistoricalConsumptionScreen(
    state: HistoricalConsumptionUiState,
    isSignedIn: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (HistoricalConsumption) -> Unit,
    onDelete: (HistoricalConsumption) -> Unit,
    onMonthChanged: (String) -> Unit,
    onYearChanged: (String) -> Unit,
    onUnitsChanged: (String) -> Unit,
    onBillChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismissEditor: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onRetryLoad: () -> Unit,
    onRetrySync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val busy = state.isSaving || state.isDeleting
    val trendsByPeriod = state.trends.associateBy { it.year to it.month }
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack, enabled = !busy) { Text("Back") }
            Text("Electricity consumption history", style = MaterialTheme.typography.headlineSmall)
        }
        if (!isSignedIn) {
            item { Text("Your session has expired. Please sign in again.") }
        } else {
            item {
                Text("Actual monthly usage and bill amounts from your electricity bills.")
                Button(onClick = onAdd, enabled = !state.isLoading && !busy) { Text("Add month") }
            }
            item { SyncStatus(state, onRetrySync) }
            if (!state.showEditor && state.recordPendingDeletion == null) {
                state.errorMessage?.let { message ->
                    item {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onRetryLoad, enabled = !busy && !state.isLoading) {
                            Text("Reload saved history")
                        }
                    }
                }
            }
            when {
                state.isLoading -> item {
                    CircularProgressIndicator()
                    Text("Loading saved history...")
                }
                state.records.isEmpty() -> item {
                    Text("No monthly consumption recorded yet. Add a month from your electricity bill.")
                }
                else -> items(
                    state.records.sortedWith(compareByDescending<HistoricalConsumption> { it.year }.thenByDescending { it.month }),
                    key = { it.id }
                ) { record ->
                    HistoryItem(record, trendsByPeriod[record.year to record.month], !busy, onEdit, onDelete)
                }
            }
        }
    }
    if (!isSignedIn) return
    if (state.showEditor) {
        HistoryEditor(state, onMonthChanged, onYearChanged, onUnitsChanged, onBillChanged, onSave, onDismissEditor)
    }
    state.recordPendingDeletion?.let { record ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Delete monthly record?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Delete the consumption record for ${periodLabel(record)}? You can add this month again later.")
                    if (state.isDeleting) {
                        CircularProgressIndicator()
                        Text("Deleting on this device...")
                    }
                    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete, enabled = !busy) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete, enabled = !busy) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SyncStatus(state: HistoricalConsumptionUiState, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        when {
            state.isSyncing -> {
                CircularProgressIndicator()
                Text("Syncing with cloud...")
            }
            state.syncErrorMessage != null -> Text(state.syncErrorMessage, color = MaterialTheme.colorScheme.error)
            state.hasSynced -> Text("Cloud sync finished.")
            else -> Text("Cloud sync has not completed yet.")
        }
        TextButton(onClick = onRetry, enabled = !state.isSyncing) {
            Text(if (state.syncErrorMessage != null) "Retry cloud sync" else "Sync now")
        }
    }
}

@Composable
private fun HistoryItem(
    record: HistoricalConsumption,
    trend: ConsumptionTrend?,
    enabled: Boolean,
    onEdit: (HistoricalConsumption) -> Unit,
    onDelete: (HistoricalConsumption) -> Unit
) {
    val number = NumberFormat.getNumberInstance(Locale("en", "PK")).apply { maximumFractionDigits = 2 }
    val money = NumberFormat.getNumberInstance(Locale("en", "PK")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(periodLabel(record), style = MaterialTheme.typography.titleMedium)
            Text("${number.format(record.unitsConsumed)} kWh")
            Text("Bill: PKR ${money.format(record.billAmount)}")
            Text(trendDescription(trend), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onEdit(record) }, enabled = enabled) { Text("Edit") }
                TextButton(onClick = { onDelete(record) }, enabled = enabled) { Text("Delete") }
            }
        }
    }
}

private fun periodLabel(record: HistoricalConsumption): String {
    return periodLabel(record.month, record.year)
}

private fun periodLabel(month: Int, year: Int): String {
    val name = DateFormatSymbols.getInstance(Locale.ENGLISH).months.getOrNull(month - 1)
        ?: month.toString()
    return "$name ${year.toString().padStart(4, '0')}"
}

private fun trendDescription(trend: ConsumptionTrend?): String {
    if (trend == null || trend.direction == TrendDirection.NOT_AVAILABLE) {
        return "No previous-month data"
    }
    val previousMonth = if (trend.month == 1) 12 else trend.month - 1
    val previousYear = if (trend.month == 1) trend.year - 1 else trend.year
    val previousPeriod = periodLabel(previousMonth, previousYear)
    if (trend.direction == TrendDirection.UNCHANGED) {
        return "No consumption change from $previousPeriod"
    }
    val change = trend.absoluteChange?.takeIf { it.isFinite() }
        ?: return "Consumption comparison unavailable"
    val increased = trend.direction == TrendDirection.INCREASED
    val arrow = if (increased) "↑" else "↓"
    val direction = if (increased) "increased" else "decreased"
    val percentage = trend.percentageChange?.takeIf { it.isFinite() }
        ?.let { " by ${formatTrendMagnitude(it)}%" }.orEmpty()
    val quantity = if (increased) "more" else "less"
    return "$arrow Consumption $direction$percentage from $previousPeriod\n" +
        "${formatTrendMagnitude(change)} kWh $quantity"
}

private fun formatTrendMagnitude(value: Double): String {
    val magnitude = abs(value)
    // Do not round a small nonzero change into an apparent zero change.
    if (magnitude > 0.0 && magnitude < 0.01) return "<0.01"
    return NumberFormat.getNumberInstance(Locale("en", "PK"))
        .apply { maximumFractionDigits = 2 }.format(magnitude)
}

@Composable
private fun HistoryEditor(
    state: HistoricalConsumptionUiState,
    onMonthChanged: (String) -> Unit,
    onYearChanged: (String) -> Unit,
    onUnitsChanged: (String) -> Unit,
    onBillChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val editing = state.editingRecordId != null
    val enabled = !state.isSaving && !state.isDeleting
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing) "Edit monthly consumption" else "Add monthly consumption") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (editing) Text("Month and year are fixed for this record.")
                HistoryField("Month (1–12)", state.month, state.fieldErrors.month, enabled && !editing, KeyboardType.Number, onMonthChanged)
                HistoryField("Year", state.year, state.fieldErrors.year, enabled && !editing, KeyboardType.Number, onYearChanged)
                HistoryField("Units consumed (kWh)", state.unitsConsumed, state.fieldErrors.unitsConsumed, enabled, KeyboardType.Decimal, onUnitsChanged)
                HistoryField("Bill amount (PKR)", state.billAmount, state.fieldErrors.billAmount, enabled, KeyboardType.Decimal, onBillChanged)
                state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.isSaving) {
                    CircularProgressIndicator()
                    Text("Saving on this device...")
                }
            }
        },
        confirmButton = { TextButton(onClick = onSave, enabled = enabled) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = enabled) { Text("Cancel") } }
    )
}

@Composable
private fun HistoryField(
    label: String,
    value: String,
    error: String?,
    enabled: Boolean,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}
