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
import com.example.mockmate.model.ParagraphQuestion
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Generates questions based on paragraph text using a simulated AI.
 */
import com.example.mockmate.api.GeminiApiService

class AIQuestionGenerator(private val geminiApiService: GeminiApiService) {
    private val TAG = "AIQuestionGenerator"
    private val gson = Gson()
    /**
     * Generates questions based on the provided paragraph using a simulated AI.
     * Requires network connectivity and an API key to attempt generation.
     * Returns an empty list if AI generation is not possible or fails.
     */
    suspend fun generateQuestionsFromParagraph(
        paragraph: String,
        numQuestions: Int,
        apiKey: String?, // Added apiKey parameter
        context: Context? = null
    ): List<ParagraphQuestion> = withContext(Dispatchers.IO) {
        // Check network availability if context is provided
        val isNetworkAvailable = context?.let {
            val connectivityManager = it.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return@let false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@let false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false

        if (!isNetworkAvailable || apiKey.isNullOrEmpty()) {
            Log.w(TAG, "AI question generation not possible: Network unavailable or API key missing.")
            return@withContext emptyList<ParagraphQuestion>() // Return empty list if AI generation is not possible
        }

        try {
            Log.d(TAG, "Network available, API key present. Calling Gemini API to generate questions.")

            val prompt = "Generate $numQuestions multiple choice questions based on the following paragraph: $paragraph"
            val geminiRequest = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = prompt
                            )
                        )
                    )
                )
            )
            val response = geminiApiService.generateContent(apiKey ?: "", geminiRequest)

            if (response.isSuccessful) {
                val content = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                Log.d(TAG, "Gemini API response: $content")

                if (content != null) {
                    // Try parsing as JSON array first
                    try {
                        val generatedQuestions = gson.fromJson(content, Array<ParagraphQuestion>::class.java).toList()
                        return@withContext generatedQuestions
                    } catch (e: JsonSyntaxException) {
                        Log.w(TAG, "Failed to parse as JSON array, attempting line-based parsing", e)

                        // If JSON parsing fails, try splitting by lines
                        val questions = content.split("\n").mapIndexed { index, line ->
                            ParagraphQuestion(
                                id = index.toString(),
                                questionText = line.trim(),
                                options = listOf("Option A", "Option B", "Option C", "Option D"), // Provide default options
                                correctOptionIndex = 0 // Provide a default correct option
                            )
                        }
                        return@withContext questions
                    }
                } else {
                    Log.w(TAG, "Gemini API response content is null")
                    return@withContext emptyList<ParagraphQuestion>()
                }
            } else {
                Log.e(TAG, "Gemini API call failed with code: ${response.code()}")
                return@withContext emptyList<ParagraphQuestion>()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Gemini API question generation: ${e.message}", e)
            if (e is JsonSyntaxException) {
                Log.e(TAG, "Error parsing Gemini API response", e)
            }
            return@withContext emptyList<ParagraphQuestion>()
        }
    }
}
