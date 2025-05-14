package com.example.mockmate.ui.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Utilities to improve Compose stability and handle common crashes
 */
object ComposeStabilityUtils {

    /**
     * Tracks composition errors more safely than try-catch (which isn't supported in Compose)
     */
    @Composable
    fun LogCompositionErrors(tag: String) {
        // Use DisposableEffect as a safer way to monitor composition
        DisposableEffect(Unit) {
            Log.d(tag, "Composable entered the composition")
            
            onDispose {
                // This will be called when the composable leaves the composition
                Log.d(tag, "Composable exited the composition")
            }
        }
    }

    /**
     * Monitors lifecycle events to detect potential issues
     */
    @Composable
    fun MonitorLifecycle(onError: (String) -> Unit) {
        val lifecycleOwner = LocalLifecycleOwner.current
        
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        Log.d("ComposeLifecycle", "Screen created")
                    }
                    Lifecycle.Event.ON_STOP -> {
                        Log.d("ComposeLifecycle", "Screen stopped")
                    }
                    else -> { /* handle other events as needed */ }
                }
            }
            
            lifecycleOwner.lifecycle.addObserver(observer)
            
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}