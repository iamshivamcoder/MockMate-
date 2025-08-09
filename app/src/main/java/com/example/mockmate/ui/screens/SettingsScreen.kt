package com.example.mockmate.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import androidx.compose.material3.HorizontalDivider
import com.example.mockmate.MockMateApplication
import com.example.mockmate.data.SettingsRepository
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.SectionHeader
import com.example.mockmate.ui.components.SettingsItem
import com.example.mockmate.ui.components.SettingsSwitch

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsRepository: SettingsRepository = MockMateApplication.getSettingsRepository()
) {
    val settings by settingsRepository.settings.collectAsState(initial = com.example.mockmate.model.AppSettings())
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // State to store the time temporarily
    var reminderTimeInput by remember { mutableStateOf(settings.reminderTime) }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MockMateTopBar(
            title = "Settings",
            showBackButton = true,
            onBackClick = onNavigateBack,
            showSettings = false
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            SectionHeader(text = "Appearance")
            
            SettingsSwitch(
                title = "Dark Mode",
                description = "Toggle dark mode theme",
                checked = settings.darkMode,
                onCheckedChange = { settingsRepository.updateDarkMode(it) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            SectionHeader(text = "Notifications")
            
            SettingsSwitch(
                title = "Enable Notifications",
                description = "Get reminders for daily practice",
                checked = settings.notificationsEnabled,
                onCheckedChange = { settingsRepository.updateNotificationsEnabled(it) }
            )
            
            SettingsItem(
                title = "Reminder Time",
                value = settings.reminderTime,
                onItemClick = { showTimePickerDialog = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "MockMate v1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }
        
        // Time Picker Dialog
        if (showTimePickerDialog) {
            TimePickerDialog(
                initialTime = settings.reminderTime,
                onDismiss = { showTimePickerDialog = false },
                onTimeSelected = { time ->
                    reminderTimeInput = time
                    settingsRepository.updateReminderTime(time)
                    showTimePickerDialog = false
                    Toast.makeText(context, "Reminder time set to $time", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    var timeInput by remember { mutableStateOf(initialTime) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Set Reminder Time",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text("Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    TextButton(
                        onClick = { onTimeSelected(timeInput) }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
