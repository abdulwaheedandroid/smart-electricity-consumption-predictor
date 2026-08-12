package com.abdulwaheed.smartelectricitypredictor.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onViewProfile: () -> Unit,
    onViewAppliances: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Welcome to Smart Electricity Predictor.",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Dashboard data will appear here as future modules are implemented.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = onViewAppliances,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("My appliances")
        }
        Button(
            onClick = onViewProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View profile")
        }
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign out")
        }
    }
}

