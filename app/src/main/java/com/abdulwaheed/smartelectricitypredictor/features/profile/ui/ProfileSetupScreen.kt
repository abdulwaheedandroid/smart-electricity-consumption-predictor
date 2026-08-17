package com.abdulwaheed.smartelectricitypredictor.features.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdulwaheed.smartelectricitypredictor.features.profile.state.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    state: ProfileUiState,
    onFullNameChanged: (String) -> Unit,
    onAgeChanged: (String) -> Unit,
    onGenderChanged: (String) -> Unit,
    onCellNumberChanged: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onRequestDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading profile...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (state.profileExists) "Your profile" else "Complete your profile",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (state.profileExists) "Review or update your profile information."
            else "Enter your profile information before continuing.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ReadOnlyProfileField("Email", state.email, state)
        OutlinedTextField(
            value = state.fullName,
            onValueChange = onFullNameChanged,
            label = { Text("Full name") },
            enabled = !state.isSaving && !state.isDeleting,
            isError = state.fieldErrors.fullName != null,
            supportingText = { state.fieldErrors.fullName?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.age,
            onValueChange = onAgeChanged,
            label = { Text("Age") },
            enabled = !state.isSaving && !state.isDeleting,
            isError = state.fieldErrors.age != null,
            supportingText = { state.fieldErrors.age?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        GenderDropdown(
            value = state.gender,
            onValueChanged = onGenderChanged,
            enabled = !state.isSaving && !state.isDeleting,
            errorMessage = state.fieldErrors.gender
        )
        OutlinedTextField(
            value = state.cellNumber,
            onValueChange = onCellNumberChanged,
            label = { Text("Cell number") },
            enabled = !state.isSaving && !state.isDeleting,
            isError = state.fieldErrors.cellNumber != null,
            supportingText = { state.fieldErrors.cellNumber?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onRetry,
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry")
            }
        }
        Button(
            onClick = onSave,
            enabled = !state.isSaving && !state.isDeleting && state.errorMessage == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) CircularProgressIndicator()
            else Text(if (state.profileExists) "Update profile" else "Create profile")
        }
        if (state.profileExists) {
            Button(
                onClick = onRequestDelete,
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isDeleting) CircularProgressIndicator() else Text("Delete profile")
            }
        }
        Button(
            onClick = onSignOut,
            enabled = !state.isSaving && !state.isDeleting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign out")
        }
    }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Delete profile?") },
            text = {
                Text(
                    "This deletes your Firestore profile data only. " +
                        "Your Firebase Authentication account will remain active."
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) { Text("Delete profile") }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(
    value: String,
    onValueChanged: (String) -> Unit,
    enabled: Boolean,
    errorMessage: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Male", "Female", "Other", "Prefer not to say")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled,
            isError = errorMessage != null,
            supportingText = { errorMessage?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyProfileField(label: String, value: String, state: ProfileUiState) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = !state.isSaving && !state.isDeleting,
        modifier = Modifier.fillMaxWidth()
    )
}
