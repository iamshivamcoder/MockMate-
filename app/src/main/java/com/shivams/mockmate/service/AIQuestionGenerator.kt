package com.shivams.mockmate.service

import com.google.gson.Gson

/**
 * Generates questions based on paragraph text using a simulated AI.
 */
import com.shivams.mockmate.api.GeminiApiService

class AIQuestionGenerator(private val geminiApiService: GeminiApiService) {
    private val TAG = "AIQuestionGenerator"
    private val gson = Gson()
}
