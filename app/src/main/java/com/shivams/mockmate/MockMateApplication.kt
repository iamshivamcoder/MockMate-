package com.shivams.mockmate

import android.app.Application
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MockMateApplication : Application() {

    @Inject
    lateinit var testRepository: TestRepository

    override fun onCreate() {
        super.onCreate()
        
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (BuildConfig.SAMPLE_DATA_ENABLED) {
                        testRepository.initializeIfEmpty()
                        android.util.Log.d("MockMateApp", "Repository initialized with sample data")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MockMateApp", "Repository initialization error: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MockMateApp", "Application initialization error: ${e.message}", e)
        }
    }
}
