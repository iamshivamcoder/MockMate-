package com.shivams.mockmate

import android.app.Application
import com.shivams.mockmate.api.GeminiApiService
import com.shivams.mockmate.data.repositories.SettingsRepository
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.data.repositories.TestRepositoryImpl
import com.shivams.mockmate.service.AIQuestionGenerator
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MockMateApplication : Application() {
    
    // Repositories
    private lateinit var testRepository: TestRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var apiConfig: ApiConfig
    private lateinit var aiQuestionGenerator: AIQuestionGenerator
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        
        // Initialize repositories
        testRepository = TestRepositoryImpl(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        apiConfig = ApiConfig(applicationContext)

        // Initialize services
        aiQuestionGenerator = AIQuestionGenerator(apiConfig.geminiApiService)
        
        // Initialize with error handling
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (BuildConfig.SAMPLE_DATA_ENABLED) { // Added condition
                        testRepository.initializeIfEmpty() // Enable sample data loading for debugging
                        android.util.Log.d("MockMateApp", "Repository initialized with sample data")
                    }
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
            return INSTANCE ?: throw IllegalStateException("Application instance not initialized")
        }
        
        fun setInstance(application: MockMateApplication) {
            INSTANCE = application
        }
        
        fun getTestRepository(): TestRepository {
            val instance = INSTANCE ?: throw IllegalStateException("Application instance not initialized")
            return instance.testRepository
        }
        
        fun getSettingsRepository(): SettingsRepository {
            val instance = INSTANCE ?: throw IllegalStateException("Application instance not initialized")
            return instance.settingsRepository
        }
        
        fun getApiConfig(): ApiConfig {
            val instance = INSTANCE ?: throw IllegalStateException("Application instance not initialized")
            return instance.apiConfig
        }
        
        fun getAIQuestionGenerator(): AIQuestionGenerator {
            val instance = INSTANCE ?: throw IllegalStateException("Application instance not initialized")
            return instance.aiQuestionGenerator
        }

        fun getGeminiApiService(): GeminiApiService {
            val instance = INSTANCE ?: throw IllegalStateException("Application instance not initialized")
            return instance.apiConfig.geminiApiService
        }
    }
}
