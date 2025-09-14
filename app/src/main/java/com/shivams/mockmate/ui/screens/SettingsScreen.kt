package com.shivams.mockmate.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.data.repositories.SettingsRepository
import com.shivams.mockmate.data.prefs.NotificationPreferences
import com.shivams.mockmate.notifications.TestReminderReceiver
import com.shivams.mockmate.ui.components.MockMateTopBar
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAboutDeveloper: () -> Unit,
    onNavigateToHelp: () -> Unit,
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) } // Instantiate SettingsRepository
    val appSettings by settingsRepository.settings.collectAsState() // Collect settings

    var notificationsEnabled by remember {
        mutableStateOf(NotificationPreferences.areNotificationsEnabled(context))
    }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("To schedule daily reminders, MockMate needs the 'Alarms & Reminders' permission. Please grant this permission in the app settings.") },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = android.net.Uri.fromParts("package", context.packageName, null)
                            fallbackIntent.data = uri
                            context.startActivity(fallbackIntent)
                        }
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MockMateTopBar(
                title = "Settings",
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "General",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "Daily Reminders",
                    subtitle = "Get 3 daily Hinglish motivation nudges",
                    onClick = {
                        val newCheckedState = !notificationsEnabled
                        if (newCheckedState) {
                            val scheduledSuccessfully = TestReminderReceiver.scheduleTestReminder(context)
                            if (scheduledSuccessfully) {
                                notificationsEnabled = true
                                NotificationPreferences.setNotificationsEnabled(context, true)
                            } else {
                                notificationsEnabled = false
                                NotificationPreferences.setNotificationsEnabled(context, false)
                                showPermissionDialog = true
                            }
                        } else {
                            TestReminderReceiver.cancelTestReminder(context)
                            notificationsEnabled = false
                            NotificationPreferences.setNotificationsEnabled(context, false)
                        }
                    },
                    action = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    val scheduledSuccessfully = TestReminderReceiver.scheduleTestReminder(context)
                                    if (scheduledSuccessfully) {
                                        notificationsEnabled = true
                                        NotificationPreferences.setNotificationsEnabled(context, true)
                                    } else {
                                        notificationsEnabled = false
                                        NotificationPreferences.setNotificationsEnabled(context, false)
                                        showPermissionDialog = true
                                    }
                                } else {
                                    TestReminderReceiver.cancelTestReminder(context)
                                    notificationsEnabled = false
                                    NotificationPreferences.setNotificationsEnabled(context, false)
                                }
                            }
                        )
                    }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Preferences Section
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.ToggleOn, // Or Icons.Filled.Animation / Icons.Filled.Visibility
                    title = "Pulsating Badges",
                    subtitle = "Enable or disable badge animations",
                    onClick = {
                        settingsRepository.setPulsatingBadgesEnabled(!appSettings.pulsatingBadgesEnabled)
                    },
                    action = {
                        Switch(
                            checked = appSettings.pulsatingBadgesEnabled,
                            onCheckedChange = { isChecked ->
                                settingsRepository.setPulsatingBadgesEnabled(isChecked)
                            }
                        )
                    }
                )
            }


            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = "Support & About",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help & FAQ",
                    subtitle = "Find answers and guides",
                    onClick = onNavigateToHelp
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About Developer",
                    subtitle = "Information about the app and developer",
                    onClick = onNavigateToAboutDeveloper
                )
            }
        }
    }
}


@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        action?.invoke()
    }
}
