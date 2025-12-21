package com.shivams.mockmate.ui.util

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
                Log.d(tag, "Composable exited the composition")
            }
        }
    }

    /**
     * Monitors lifecycle events to detect potential issues
     */
    @Composable
    fun MonitorLifecycle(onError: (String) -> Unit) {
        @Suppress("DEPRECATION")
        val lifecycleOwner = LocalLifecycleOwner.current
        
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                try {
                    when (event) {
                        Lifecycle.Event.ON_CREATE -> {
                            Log.d("ComposeLifecycle", "Screen created")
                        }

                        Lifecycle.Event.ON_STOP -> {
                            Log.d("ComposeLifecycle", "Screen stopped")
                        }

                        Lifecycle.Event.ON_DESTROY -> {
                            Log.d("ComposeLifecycle", "Screen destroyed")
                        }

                        else -> {
                            // Log other events for better debugging
                            Log.d("ComposeLifecycle", "Lifecycle event: $event")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ComposeLifecycle", "Error in lifecycle observer: ${e.message}", e)
                    onError("Lifecycle error: ${e.message}")
                }
            }

            try {
                lifecycleOwner.lifecycle.addObserver(observer)
            } catch (e: Exception) {
                Log.e("ComposeLifecycle", "Failed to add lifecycle observer: ${e.message}", e)
                onError("Failed to add lifecycle observer: ${e.message}")
            }
            
            onDispose {
                try {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                } catch (e: Exception) {
                    Log.e(
                        "ComposeLifecycle",
                        "Failed to remove lifecycle observer: ${e.message}",
                        e
                    )
                }
            }
        }
    }
}
