package com.shivams.mockmate

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.shivams.mockmate.api.GeminiApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig(context: Context) {
    private val BASE_URL = "https://generativelanguage.googleapis.com/"

    val geminiApiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("api_config", Context.MODE_PRIVATE)

    private val API_KEYS_MAP = "api_keys_map"
    private val gson = Gson()

    // Stores a map of provider name to API key
    private var apiKeys: MutableMap<String, String> = loadApiKeys().toMutableMap()

    val hasApiKeys: Boolean
        get() = apiKeys.isNotEmpty()

    fun getApiKey(providerName: String): String? {
        return apiKeys[providerName]
    }

    fun saveApiKey(providerName: String, apiKey: String) {
        apiKeys[providerName] = apiKey
        saveApiKeys(apiKeys)
    }

    fun getAllApiKeys(): Map<String, String> {
        return apiKeys.toMap()
    }

    private fun loadApiKeys(): Map<String, String> {
        val json = sharedPreferences.getString(API_KEYS_MAP, null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<Map<String, String>>() {}.type) ?: emptyMap()
        } else {
            emptyMap()
        }
    }

    private fun saveApiKeys(keys: Map<String, String>) {
        val json = gson.toJson(keys)
        sharedPreferences.edit {
            putString(API_KEYS_MAP, json)
        }
    }
}
