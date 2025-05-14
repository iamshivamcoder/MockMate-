package com.example.mockmate.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mockmate.MockMateApplication
import com.example.mockmate.data.SettingsRepository
import com.example.mockmate.model.TestDifficulty
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
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
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
                onItemClick = { /* Time picker would go here */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SectionHeader(text = "Mock Test Preferences")
            
            SettingsItem(
                title = "Default Difficulty",
                value = settings.defaultTestDifficulty.name,
                onItemClick = { /* Difficulty selection would go here */ }
            )
            
            SettingsSwitch(
                title = "Show Explanations",
                description = "Show explanations after answering questions",
                checked = settings.showExplanations,
                onCheckedChange = { settingsRepository.updateShowExplanations(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SectionHeader(text = "UPSC Specific")
            
            SettingsSwitch(
                title = "Current Affairs Updates",
                description = "Receive daily current affairs updates",
                checked = false,
                onCheckedChange = { /* Current affairs updates toggle */ }
            )
            
            SettingsItem(
                title = "Select Optional Subject",
                value = "Not Selected",
                onItemClick = { /* Optional subject selection would go here */ }
            )
            
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
    }
}