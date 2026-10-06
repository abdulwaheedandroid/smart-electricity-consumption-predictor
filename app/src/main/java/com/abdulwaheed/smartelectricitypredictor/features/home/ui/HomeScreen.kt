package com.abdulwaheed.smartelectricitypredictor.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulwaheed.smartelectricitypredictor.features.home.state.HomeUiState
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onViewProfile: () -> Unit,
    onViewAppliances: () -> Unit,
    onViewHistory: () -> Unit,
    onSignOut: () -> Unit,
    isSigningOut: Boolean = false,
    signOutError: String? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
                Text("Welcome to Smart Electricity Predictor.", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Estimates based on current appliance settings. Actual usage from electricity bills is stored separately in history.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        when {
            state.isLoading -> item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator()
                    Text("Loading appliance estimates...")
                }
            }
            state.errorMessage != null -> item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry, enabled = !isSigningOut) { Text("Retry") }
                }
            }
            else -> {
                item { ConsumptionSummary(state) }
                if (state.applianceEstimates.isEmpty()) {
                    item { Text("No appliances added yet. Add your appliances to calculate estimates.") }
                } else {
                    item { Text("Estimated daily consumption per appliance", style = MaterialTheme.typography.titleMedium) }
                    items(state.applianceEstimates, key = { it.id }) { estimate ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(estimate.name, style = MaterialTheme.typography.titleMedium)
                                Text("${formatKwh(estimate.dailyKwh)} kWh/day (estimated)")
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onViewAppliances, enabled = !isSigningOut, modifier = Modifier.fillMaxWidth()) {
                    Text("My appliances")
                }
                Button(onClick = onViewHistory, enabled = !isSigningOut, modifier = Modifier.fillMaxWidth()) {
                    Text("Electricity consumption history")
                }
                Button(onClick = onViewProfile, enabled = !isSigningOut, modifier = Modifier.fillMaxWidth()) {
                    Text("View profile")
                }
                signOutError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = onSignOut, enabled = !isSigningOut, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isSigningOut) "Signing out..." else "Sign out")
                }
            }
        }
    }
}

@Composable
private fun ConsumptionSummary(state: HomeUiState) {
    val month = DateFormatSymbols.getInstance(Locale.ENGLISH).months[state.month - 1]
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Estimated consumption", style = MaterialTheme.typography.titleMedium)
            Text("Daily: ${formatKwh(state.totalDailyKwh)} kWh")
            Text("$month ${state.year}: ${formatKwh(state.monthlyKwh)} kWh")
            Text("Assumes the current settings apply every day for all ${state.daysInMonth} days of this month.")
        }
    }
}

private fun formatKwh(value: Double): String = NumberFormat.getNumberInstance(Locale("en", "PK"))
    .apply { maximumFractionDigits = 3 }.format(value)

