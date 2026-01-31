package com.shivams.mockmate.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shivams.mockmate.ApiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ApiTimeoutTest {

    @Test
    fun `verify OkHttpClient timeout settings are 120 seconds`() {
        // Access the internal okHttpClient from ApiModule
        val client = ApiModule.okHttpClient
        
        // 120 seconds in milliseconds
        val expectedTimeoutMillis = 120_000
        
        assertEquals("Connect timeout should be 120s", expectedTimeoutMillis, client.connectTimeoutMillis)
        assertEquals("Read timeout should be 120s", expectedTimeoutMillis, client.readTimeoutMillis)
        assertEquals("Write timeout should be 120s", expectedTimeoutMillis, client.writeTimeoutMillis)
    }

    @Test
    fun `verify ApiConfig uses ApiModule geminiApiService`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val apiConfig = ApiConfig(context)
        
        // Verify ApiConfig provides the service instance from ApiModule
        // Since ApiModule uses lazy initialization, accessing it here initializes it
        val serviceFromConfig = apiConfig.geminiApiService
        val serviceFromModule = ApiModule.geminiApiService
        
        // This confirms ApiConfig delegates to ApiModule and doesn't create a new instance with default timeouts
        assertEquals("ApiConfig should use the centralized ApiModule service", serviceFromModule, serviceFromConfig)
    }
}
