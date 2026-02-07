package com.shivams.mockmate.ui.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * Haptic feedback patterns for different user interactions.
 * Provides tactile reinforcement for learning outcomes.
 */
object HapticFeedbackManager {
    
    /**
     * Success haptic - crisp, sharp feedback for correct answers
     * Creates a satisfying "confirmation" feel
     */
    fun success(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
    
    /**
     * Success haptic using Compose's HapticFeedback
     */
    fun success(hapticFeedback: HapticFeedback) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Failure haptic - double buzz for wrong answers
     * Creates a distinct "rejection" feel
     */
    fun failure(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            // Fall back to long press for older devices
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            // Schedule second buzz after short delay
            view.postDelayed({
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }, 100)
        }
    }
    
    /**
     * Failure haptic using Compose's HapticFeedback
     */
    fun failure(hapticFeedback: HapticFeedback) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Selection haptic - light tick for selections/scrolling
     * Very subtle, used for list item selection or dial scrolling
     */
    fun selection(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
    
    /**
     * Selection haptic using Compose's HapticFeedback  
     */
    fun selection(hapticFeedback: HapticFeedback) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Heavy click for important button presses
     */
    fun heavyClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}

/**
 * Composable wrapper to easily access haptic feedback in Compose
 */
@Composable 
fun rememberHapticFeedback(): HapticFeedback = LocalHapticFeedback.current

/**
 * Composable wrapper to access View for advanced haptics
 */
@Composable
fun rememberViewForHaptics(): View = LocalView.current
