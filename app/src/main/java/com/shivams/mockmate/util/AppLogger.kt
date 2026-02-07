package com.shivams.mockmate.util

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.shivams.mockmate.BuildConfig

/**
 * Production-safe logging wrapper.
 * Logs are only printed in DEBUG builds to prevent sensitive information leakage.
 * Errors are automatically reported to Firebase Crashlytics in production.
 */
object AppLogger {
    private const val DEFAULT_TAG = "MockMate"
    
    /**
     * Log debug messages - only shown in DEBUG builds
     */
    fun d(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Log info messages - only shown in DEBUG builds
     */
    fun i(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }
    
    /**
     * Log warning messages - only shown in DEBUG builds
     */
    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.w(tag, message, throwable)
            } else {
                Log.w(tag, message)
            }
        }
    }
    
    /**
     * Log error messages - shown in both DEBUG and RELEASE builds
     * Errors are sent to Firebase Crashlytics in production
     */
    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        } else {
            // In release, log to Crashlytics
            try {
                Firebase.crashlytics.log("$tag: $message")
                if (throwable != null) {
                    Firebase.crashlytics.recordException(throwable)
                }
            } catch (e: Exception) {
                // Crashlytics not initialized, ignore
            }
        }
    }
    
    /**
     * Log verbose messages - only shown in DEBUG builds
     */
    fun v(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message)
        }
    }
    
    /**
     * Record a non-fatal exception to Crashlytics
     */
    fun recordException(throwable: Throwable) {
        if (!BuildConfig.DEBUG) {
            try {
                Firebase.crashlytics.recordException(throwable)
            } catch (e: Exception) {
                // Crashlytics not initialized, ignore
            }
        } else {
            Log.e(DEFAULT_TAG, "Exception recorded", throwable)
        }
    }
    
    /**
     * Set a custom key-value pair for Crashlytics context
     */
    fun setCustomKey(key: String, value: String) {
        if (!BuildConfig.DEBUG) {
            try {
                Firebase.crashlytics.setCustomKey(key, value)
            } catch (e: Exception) {
                // Crashlytics not initialized, ignore
            }
        }
    }
}

