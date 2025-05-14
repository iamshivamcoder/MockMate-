package com.example.mockmate

import android.app.Application
import com.example.mockmate.data.InMemoryTestRepository
import com.example.mockmate.data.SettingsRepository
import com.example.mockmate.data.TestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MockMateApplication : Application() {
    
    // Repositories
    private val testRepository: TestRepository = InMemoryTestRepository()
    private val settingsRepository: SettingsRepository = SettingsRepository()
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        
        // Initialize with error handling
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    testRepository.initializeIfEmpty()
                } catch (e: Exception) {
                    // Log and handle initialization errors
                    android.util.Log.e("MockMateApp", "Repository initialization error: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            // Log application level errors
            android.util.Log.e("MockMateApp", "Application initialization error: ${e.message}", e)
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: MockMateApplication? = null
        
        fun getInstance(): MockMateApplication {
            return INSTANCE!!
        }
        
        fun setInstance(application: MockMateApplication) {
            INSTANCE = application
        }
        
        fun getTestRepository(): TestRepository {
            // Add null-check for safer access
            val instance = INSTANCE ?: throw IllegalStateException("Application instance not initialized")
            return instance.testRepository
        }
        
        fun getSettingsRepository(): SettingsRepository {
            // Add null-check for safer access
            val instance = INSTANCE ?: throw IllegalStateException("Application instance not initialized")
            return instance.settingsRepository
        }
    }
}