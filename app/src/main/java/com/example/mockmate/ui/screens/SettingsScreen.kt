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
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showOptionalSubjectDialog by remember { mutableStateOf(false) }
    
    // State to store the time temporarily
    var reminderTimeInput by remember { mutableStateOf(settings.reminderTime) }
    // Current optional subject
    var optionalSubject by remember { mutableStateOf(settingsRepository.getOptionalSubject()) }
    // Current affairs state
    var currentAffairsEnabled by remember { mutableStateOf(settingsRepository.getCurrentAffairsUpdates()) }
    
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
                onItemClick = { showTimePickerDialog = true }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SectionHeader(text = "Mock Test Preferences")
            
            SettingsItem(
                title = "Default Difficulty",
                value = settings.defaultTestDifficulty.name,
                onItemClick = { showDifficultyDialog = true }
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
                checked = currentAffairsEnabled,
                onCheckedChange = { 
                    currentAffairsEnabled = it
                    settingsRepository.updateCurrentAffairsUpdates(it)
                    Toast.makeText(context, "Current affairs updates ${if(it) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show() 
                }
            )
            
            SettingsItem(
                title = "Select Optional Subject",
                value = optionalSubject,
                onItemClick = { showOptionalSubjectDialog = true }
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
        
        // Difficulty Selection Dialog
        if (showDifficultyDialog) {
            DifficultySelectionDialog(
                currentDifficulty = settings.defaultTestDifficulty,
                onDismiss = { showDifficultyDialog = false },
                onDifficultySelected = { difficulty ->
                    settingsRepository.updateDefaultTestDifficulty(difficulty)
                    showDifficultyDialog = false
                    Toast.makeText(context, "Default difficulty set to ${difficulty.name}", Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        // Optional Subject Dialog
        if (showOptionalSubjectDialog) {
            OptionalSubjectDialog(
                currentSubject = optionalSubject,
                onDismiss = { showOptionalSubjectDialog = false },
                onSubjectSelected = { subject ->
                    optionalSubject = subject
                    settingsRepository.updateOptionalSubject(subject)
                    showOptionalSubjectDialog = false
                    Toast.makeText(context, "Optional subject set to $subject", Toast.LENGTH_SHORT).show()
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

@Composable
fun DifficultySelectionDialog(
    currentDifficulty: TestDifficulty,
    onDismiss: () -> Unit,
    onDifficultySelected: (TestDifficulty) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
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
                    text = "Select Default Difficulty",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TestDifficulty.values().forEach { difficulty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDifficultySelected(difficulty) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = difficulty.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (difficulty == currentDifficulty) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun OptionalSubjectDialog(
    currentSubject: String,
    onDismiss: () -> Unit,
    onSubjectSelected: (String) -> Unit
) {
    val optionalSubjects = listOf(
        "Not Selected",
        "Agriculture",
        "Animal Husbandry & Veterinary Science",
        "Anthropology",
        "Botany",
        "Chemistry",
        "Civil Engineering",
        "Commerce & Accountancy",
        "Economics",
        "Electrical Engineering",
        "Geography",
        "Geology",
        "History",
        "Law",
        "Management",
        "Mathematics",
        "Mechanical Engineering",
        "Medical Science",
        "Philosophy",
        "Physics",
        "Political Science & International Relations",
        "Psychology",
        "Public Administration",
        "Sociology",
        "Statistics",
        "Zoology"
    )
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select Optional Subject",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                optionalSubjects.forEach { subject ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSubjectSelected(subject) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (subject == currentSubject) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}