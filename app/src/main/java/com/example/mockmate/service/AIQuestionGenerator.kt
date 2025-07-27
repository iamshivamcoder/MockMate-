package com.example.mockmate.service

import com.example.mockmate.model.Content
import com.example.mockmate.model.GeminiRequest
import com.example.mockmate.model.Part

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.JsonSyntaxException
import android.util.Log
import com.example.mockmate.ApiConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Generates questions based on paragraph text using a simulated AI.
 */
import com.example.mockmate.api.GeminiApiService

class AIQuestionGenerator(private val geminiApiService: GeminiApiService) {
    private val TAG = "AIQuestionGenerator"
    private val gson = Gson()
}
