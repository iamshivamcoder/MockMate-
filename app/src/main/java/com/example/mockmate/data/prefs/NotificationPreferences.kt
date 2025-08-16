package com.example.mockmate.data.prefs

import android.content.Context
import android.content.SharedPreferences

object NotificationPreferences {

    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true) // Default to true
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
}
