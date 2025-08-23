package com.example.mockmate

import android.app.Application
import com.example.mockmate.api.GeminiApiService
import com.example.mockmate.data.SettingsRepository
import com.example.mockmate.data.TestRepository
import com.example.mockmate.service.AIQuestionGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        testRepository = com.example.mockmate.data.TestRepositoryImpl(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        apiConfig = ApiConfig(applicationContext)

        // Initialize services
        aiQuestionGenerator = AIQuestionGenerator(apiConfig.geminiApiService)
        
        // Initialize with error handling
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    testRepository.initializeIfEmpty() // Enable sample data loading for debugging
                    android.util.Log.d("MockMateApp", "Repository initialized with sample data")
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
