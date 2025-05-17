package com.example.mockmate.data

import android.content.Context
import android.content.SharedPreferences
import com.example.mockmate.model.AppSettings
import com.example.mockmate.model.TestDifficulty
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository that manages user settings and preferences
 */
class SettingsRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    /**
     * Load settings from SharedPreferences
     */
    private fun loadSettings(): AppSettings {
        val darkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        val notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        val reminderTime = sharedPreferences.getString(KEY_REMINDER_TIME, "08:00") ?: "08:00"
        val defaultDifficultyString = sharedPreferences.getString(KEY_DEFAULT_TEST_DIFFICULTY, TestDifficulty.MEDIUM.name) ?: TestDifficulty.MEDIUM.name
        val defaultTestDifficulty = try {
            TestDifficulty.valueOf(defaultDifficultyString)
        } catch (e: Exception) {
            TestDifficulty.MEDIUM
        }
        val showExplanations = sharedPreferences.getBoolean(KEY_SHOW_EXPLANATIONS, true)
        val currentAffairsUpdates = sharedPreferences.getBoolean(KEY_CURRENT_AFFAIRS_UPDATES, false)
        val optionalSubject = sharedPreferences.getString(KEY_OPTIONAL_SUBJECT, "Not Selected") ?: "Not Selected"
        
        return AppSettings(
            darkMode = darkMode,
            notificationsEnabled = notificationsEnabled,
            reminderTime = reminderTime,
            defaultTestDifficulty = defaultTestDifficulty,
            showExplanations = showExplanations,
            currentAffairsUpdates = currentAffairsUpdates,
            optionalSubject = optionalSubject
        )
    }

    fun updateDarkMode(darkMode: Boolean) {
        _settings.value = _settings.value.copy(darkMode = darkMode)
        saveSetting(KEY_DARK_MODE, darkMode)
    }
    
    fun updateNotificationsEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
        saveSetting(KEY_NOTIFICATIONS_ENABLED, enabled)
    }
    
    fun updateReminderTime(time: String) {
        _settings.value = _settings.value.copy(reminderTime = time)
        saveSetting(KEY_REMINDER_TIME, time)
    }
    
    fun updateDefaultTestDifficulty(difficulty: TestDifficulty) {
        _settings.value = _settings.value.copy(defaultTestDifficulty = difficulty)
        saveSetting(KEY_DEFAULT_TEST_DIFFICULTY, difficulty.name)
    }
    
    fun updateShowExplanations(show: Boolean) {
        _settings.value = _settings.value.copy(showExplanations = show)
        saveSetting(KEY_SHOW_EXPLANATIONS, show)
    }
    
    fun updateCurrentAffairsUpdates(enabled: Boolean) {
        _settings.value = _settings.value.copy(currentAffairsUpdates = enabled)
        sharedPreferences.edit().putBoolean(KEY_CURRENT_AFFAIRS_UPDATES, enabled).apply()
    }
    
    fun getCurrentAffairsUpdates(): Boolean {
        return sharedPreferences.getBoolean(KEY_CURRENT_AFFAIRS_UPDATES, false)
    }
    
    fun updateOptionalSubject(subject: String) {
        _settings.value = _settings.value.copy(optionalSubject = subject)
        sharedPreferences.edit().putString(KEY_OPTIONAL_SUBJECT, subject).apply()
    }
    
    fun getOptionalSubject(): String {
        return sharedPreferences.getString(KEY_OPTIONAL_SUBJECT, "Not Selected") ?: "Not Selected"
    }
    
    /**
     * Helper methods to save settings to SharedPreferences
     */
    private fun saveSetting(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    private fun saveSetting(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "mockmate_settings"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_DEFAULT_TEST_DIFFICULTY = "default_test_difficulty"
        private const val KEY_SHOW_EXPLANATIONS = "show_explanations"
        private const val KEY_CURRENT_AFFAIRS_UPDATES = "current_affairs_updates"
        private const val KEY_OPTIONAL_SUBJECT = "optional_subject"
    }
}
