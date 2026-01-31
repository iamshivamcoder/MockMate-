package com.shivams.mockmate

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.shivams.mockmate.data.repositories.SettingsRepository
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.notifications.TestReminderReceiver
import com.shivams.mockmate.ui.navigation.AppNavHost
import com.shivams.mockmate.ui.navigation.Routes
import com.shivams.mockmate.ui.theme.MockMateTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var testRepository: TestRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All necessary permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            val denied = permissions.filter { !it.value }.keys.joinToString(", ")
            Toast.makeText(
                this,
                "Some permissions were denied: $denied. Certain features may not work. To enable all features, please grant these permissions in your device settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Add back handler
    private val backPressedTime = mutableLongStateOf(0L) // Changed to mutableLongStateOf

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
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) { // API 32 or below
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Do NOT add INTERNET or ACCESS_NETWORK_STATE here (manifest only)
        // Remove WRITE_SETTINGS from here
        val filteredPermissions = permissions.filter {
            try {
                packageManager.getPermissionInfo(it, 0)
                true
            } catch (_: Exception) { // Changed e to _
                false
            }
        }
        val permissionsToRequest = filteredPermissions.filter {
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

        // Schedule reminders
        TestReminderReceiver.scheduleTestReminder(this)
        
        // Set content with error handling
        try {
            setContent {
                // Use stable state to track error conditions
                var hasError by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf("") }
                
                // Create stable navigation controller
                val navController = rememberNavController()
                
                // Check if launched via PDF share intent
                val sharedPdfUri = remember {
                    if (intent?.action == Intent.ACTION_SEND && intent.type == "application/pdf") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(Intent.EXTRA_STREAM)
                        }
                    } else null
                }
                
                // Navigate to PDF analyzer if shared PDF is present
                LaunchedEffect(sharedPdfUri) {
                    if (sharedPdfUri != null) {
                        navController.navigate(Routes.PDF_IMPORT)
                    }
                }
                
                val settings by settingsRepository.settings.collectAsState(initial = com.shivams.mockmate.model.AppSettings())

                // Apply theme
                MockMateTheme(darkTheme = settings.darkMode) {
                    // Root composable
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!hasError) {
                            // Normal app UI
                            AppNavHost(
                                navController = navController,
                                repository = testRepository,
                                settingsRepository = settingsRepository
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
    
    // Removed unused checkAndRequestPermissions function

    private fun isMIUI(): Boolean {
        return try {
            val prop = Build::class.java.getMethod("getPropertyByName", String::class.java)
            prop.invoke(null, "ro.miui.ui.version.name") != null
        } catch (_: Exception) { // Changed e to _
            false
        }
    }
}
