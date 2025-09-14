package com.shivams.mockmate.model

import com.google.gson.annotations.SerializedName

// Request models
data class GeminiRequest(
    @SerializedName("contents") val contents: List<Content>,
    @SerializedName("generationConfig") val generationConfig: GenerationConfig = GenerationConfig()
)

data class Content(
    @SerializedName("parts") val parts: List<Part>
)

data class Part(
    @SerializedName("text") val text: String
)

data class GenerationConfig(
    @SerializedName("temperature") val temperature: Float = 0.7f,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 1024,
    @SerializedName("topP") val topP: Float = 0.95f,
    @SerializedName("topK") val topK: Int = 40
)

// Response models
data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate> = emptyList(),
    @SerializedName("promptFeedback") val promptFeedback: PromptFeedback? = null
)

data class Candidate(
    @SerializedName("content") val content: Content? = null,
    @SerializedName("finishReason") val finishReason: String? = null
)

data class PromptFeedback(
    @SerializedName("blockReason") val blockReason: String? = null
)