package com.shivams.mockmate

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.shivams.mockmate.api.GeminiApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Suppress("DEPRECATION")
class ApiConfig(context: Context) {
    companion object {
        private const val TAG = "ApiConfig"
        private const val ENCRYPTED_PREFS_NAME = "api_config_encrypted"
        private const val LEGACY_PREFS_NAME = "api_config"
        private const val API_KEYS_MAP = "api_keys_map"
    }

    val geminiApiService: GeminiApiService by lazy {
        com.shivams.mockmate.api.ApiModule.geminiApiService
    }

    private val gson = Gson()
    
    private val encryptedSharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences, falling back to regular prefs", e)
            // Fallback for older devices or crypto issues
            context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // Stores a map of provider name to API key
    private var apiKeys: MutableMap<String, String>

    init {
        // Migrate from legacy unencrypted storage if needed
        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        val legacyJson = legacyPrefs.getString(API_KEYS_MAP, null)
        
        if (legacyJson != null && encryptedSharedPreferences.getString(API_KEYS_MAP, null) == null) {
            // Migrate legacy keys to encrypted storage
            Log.d(TAG, "Migrating API keys to encrypted storage")
            encryptedSharedPreferences.edit {
                putString(API_KEYS_MAP, legacyJson)
            }
            // Clear legacy storage after migration
            legacyPrefs.edit { remove(API_KEYS_MAP) }
        }
        
        apiKeys = loadApiKeys().toMutableMap()
    }

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
        return try {
            val json = encryptedSharedPreferences.getString(API_KEYS_MAP, null)
            if (json != null) {
                gson.fromJson(json, object : TypeToken<Map<String, String>>() {}.type) ?: emptyMap()
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading API keys", e)
            emptyMap()
        }
    }

    private fun saveApiKeys(keys: Map<String, String>) {
        try {
            val json = gson.toJson(keys)
            encryptedSharedPreferences.edit {
                putString(API_KEYS_MAP, json)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving API keys", e)
        }
    }
}

