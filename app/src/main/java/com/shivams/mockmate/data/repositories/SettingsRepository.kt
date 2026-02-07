package com.shivams.mockmate.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.shivams.mockmate.data.prefs.NotificationPreferences
import com.shivams.mockmate.model.AppSettings
import com.shivams.mockmate.model.TestDifficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository that manages user settings and preferences
 */
class SettingsRepository(private val context: Context) { // Made context a property

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /**
     * Load settings from SharedPreferences
     */
    private fun loadSettings(): AppSettings {
        val darkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        val notificationsEnabled = NotificationPreferences.areNotificationsEnabled(context) // Using NotificationPreferences
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
        val pulsatingBadgesEnabled = NotificationPreferences.arePulsatingBadgesEnabled(context) // Added this line
        val immediateFeedbackEnabled = NotificationPreferences.isImmediateFeedbackEnabled(context)

        return AppSettings(
            darkMode = darkMode,
            notificationsEnabled = notificationsEnabled,
            reminderTime = reminderTime,
            defaultTestDifficulty = defaultTestDifficulty,
            showExplanations = showExplanations,
            currentAffairsUpdates = currentAffairsUpdates,
            optionalSubject = optionalSubject,
            pulsatingBadgesEnabled = pulsatingBadgesEnabled,
            immediateFeedbackEnabled = immediateFeedbackEnabled
        )
    }

    // Added this function to update pulsating badges setting
    fun setPulsatingBadgesEnabled(enabled: Boolean) {
        NotificationPreferences.setPulsatingBadgesEnabled(context, enabled)
        _settings.value = loadSettings() // Reload settings to reflect changes
    }

    // Function to toggle immediate color-coded feedback
    fun setImmediateFeedbackEnabled(enabled: Boolean) {
        NotificationPreferences.setImmediateFeedbackEnabled(context, enabled)
        _settings.value = loadSettings()
    }

    companion object {
        private const val PREFS_NAME = "mockmate_settings"
        private const val KEY_DARK_MODE = "dark_mode"
        // KEY_NOTIFICATIONS_ENABLED is now in NotificationPreferences
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_DEFAULT_TEST_DIFFICULTY = "default_test_difficulty"
        private const val KEY_SHOW_EXPLANATIONS = "show_explanations"
        private const val KEY_CURRENT_AFFAIRS_UPDATES = "current_affairs_updates"
        private const val KEY_OPTIONAL_SUBJECT = "optional_subject"
        // KEY_PULSATING_BADGES_ENABLED is now in NotificationPreferences
    }
}