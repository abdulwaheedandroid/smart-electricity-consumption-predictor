package com.abdulwaheed.smartelectricitypredictor.features.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onViewProfile: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        Text("Welcome")
        Button(onClick = onViewProfile) {
            Text("View profile")
        }
        Button(onClick = onSignOut) {
            Text("Sign out")
        }
    }
}

