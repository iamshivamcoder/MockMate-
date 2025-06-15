package com.example.mockmate.api

import com.example.mockmate.model.GeminiRequest
import com.example.mockmate.model.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
@POST("v1/models/gemini-pro:generateUPSCQuestions")
    suspend fun generateUPSCQuestions(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
    @POST("v1/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
