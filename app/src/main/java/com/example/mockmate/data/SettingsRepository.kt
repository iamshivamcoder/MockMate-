package com.example.mockmate.data

import com.example.mockmate.model.AppSettings
import com.example.mockmate.model.TestDifficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository that manages user settings and preferences
 */
class SettingsRepository {
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    fun updateDarkMode(darkMode: Boolean) {
        _settings.value = _settings.value.copy(darkMode = darkMode)
    }
    
    fun updateNotificationsEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
    }
    
    fun updateReminderTime(time: String) {
        _settings.value = _settings.value.copy(reminderTime = time)
    }
    
    fun updateDefaultTestDifficulty(difficulty: TestDifficulty) {
        _settings.value = _settings.value.copy(defaultTestDifficulty = difficulty)
    }
    
    fun updateShowExplanations(show: Boolean) {
        _settings.value = _settings.value.copy(showExplanations = show)
    }
}