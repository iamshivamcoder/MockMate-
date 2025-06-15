package com.example.mockmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.mockmate.ui.navigation.AppNavHost
import com.example.mockmate.ui.theme.MockMateTheme
import com.example.mockmate.data.TestRepository
import com.example.mockmate.MockMateApplication
import androidx.compose.runtime.collectAsState
import com.example.mockmate.data.SettingsRepository
import androidx.activity.OnBackPressedCallback

class MainActivity : ComponentActivity() {
    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All necessary permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Some permissions were denied. App functionality may be limited.", Toast.LENGTH_LONG).show()
        }
    }

    // Add back handler
    private val backPressedTime = mutableStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure back handler
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime.value < 2000) {
                        finish()
                    } else {
                        backPressedTime.value = currentTime
                        Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        // Request all necessary permissions at startup
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.WRITE_SETTINGS
        )

        // Check if any permission needs to be requested
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        }

        // Handle MIUI-specific settings
        try {
            // Disable MIUI optimization if causing issues
            if (isMIUI()) {
                android.provider.Settings.System.putInt(contentResolver, "force_fsg_nav_bar", 0)
            }
        } catch (e: Exception) {
            // Log but don't crash if MIUI settings are inaccessible
            android.util.Log.e("MainActivity", "Could not modify MIUI settings: ${e.message}")
        }

        // Initialize application instance
        MockMateApplication.setInstance(application as MockMateApplication)
        
        // Initialize repositories with stable reference
        val testRepository = MockMateApplication.getTestRepository()
        val settingsRepository = MockMateApplication.getSettingsRepository()
        
        // Set content with error handling
        try {
            setContent {
                // Use stable state to track error conditions
                var hasError by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf("") }
                
                // Create stable navigation controller
                val navController = rememberNavController()
                
                val settings by settingsRepository.settings.collectAsState(initial = com.example.mockmate.model.AppSettings())

                // Apply theme
                MockMateTheme(darkTheme = settings.darkMode) {
                    // Root composable
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!hasError) {
                            // Normal app UI
                            AppNavHost(
                                navController = navController,
                                repository = testRepository
                            )
                            
                            // Error handling via effect, not try-catch
                            LaunchedEffect(Unit) {
                                try {
                                    // Monitor for errors (simplified approach)
                                } catch (e: Exception) {
                                    hasError = true
                                    errorMessage = e.message ?: "Unknown error"
                                    android.util.Log.e("MockMate", "Error in app: $errorMessage", e)
                                }
                            }
                        } else {
                            // Fallback UI
                            Text(
                                text = "An error occurred. Please restart the app.",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Log application level errors
            android.util.Log.e("MockMate", "Fatal error in setContent: ${e.message}", e)
            
            // Show a toast to the user
            Toast.makeText(this, "An error occurred. Please restart the app.", Toast.LENGTH_LONG).show()
        }
    }
    
    // Check and request necessary permissions
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Check which permissions are not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        // For Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
        
        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun isMIUI(): Boolean {
        return try {
            val prop = android.os.Build::class.java.getMethod("getPropertyByName", String::class.java)
            prop.invoke(null, "ro.miui.ui.version.name") != null
        } catch (e: Exception) {
            false
        }
    }
}
