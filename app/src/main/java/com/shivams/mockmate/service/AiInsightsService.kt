package com.shivams.mockmate.service

import android.util.Log
import com.shivams.mockmate.ApiConfig
import com.shivams.mockmate.model.*
import com.shivams.mockmate.data.repositories.TestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Service for generating AI-powered performance insights using Gemini API
 */
class AiInsightsService(
    private val apiConfig: ApiConfig,
    private val testRepository: TestRepository
) {
    companion object {
        private const val TAG = "AiInsightsService"
        private const val GEMINI_PROVIDER = "Gemini"
    }

    /**
     * Generate comprehensive performance insights from user stats and test attempts
     */
    suspend fun generatePerformanceInsights(): Result<PerformanceInsight> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiConfig.getApiKey(GEMINI_PROVIDER)
                ?: return@withContext Result.failure(Exception("Gemini API key not configured"))

            val userStats = testRepository.userStats.first()
            val testAttempts = testRepository.getAllTestAttempts().first()

            if (userStats.questionsAnswered == 0) {
                return@withContext Result.success(createEmptyInsight())
            }

            val prompt = buildPerformanceAnalysisPrompt(userStats, testAttempts)
            val response = callGeminiApi(apiKey, prompt)
            
            parsePerformanceInsight(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating performance insights", e)
            Result.failure(e)
        }
    }

    /**
     * Generate quick insight for dashboard display
     */
    suspend fun generateQuickInsight(): Result<QuickInsight> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiConfig.getApiKey(GEMINI_PROVIDER)
                ?: return@withContext Result.failure(Exception("Gemini API key not configured"))

            val userStats = testRepository.userStats.first()
            
            if (userStats.questionsAnswered == 0) {
                return@withContext Result.success(
                    QuickInsight(
                        message = "Start practicing to get personalized insights! 📚",
                        actionPrompt = "Take your first test to begin your UPSC journey.",
                        focusArea = null
                    )
                )
            }

            val prompt = buildQuickInsightPrompt(userStats)
            val response = callGeminiApi(apiKey, prompt)
            
            parseQuickInsight(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating quick insight", e)
            Result.failure(e)
        }
    }

    /**
     * Generate test-specific insights after completing a test
     */
    suspend fun generateTestResultInsight(
        testName: String,
        score: Float,
        totalQuestions: Int,
        correctAnswers: Int,
        subjectBreakdown: Map<String, Pair<Int, Int>> // subject -> (correct, total)
    ): Result<TestResultInsight> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiConfig.getApiKey(GEMINI_PROVIDER)
                ?: return@withContext Result.failure(Exception("Gemini API key not configured"))

            val prompt = buildTestResultPrompt(testName, score, totalQuestions, correctAnswers, subjectBreakdown)
            val response = callGeminiApi(apiKey, prompt)
            
            parseTestResultInsight(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating test result insight", e)
            Result.failure(e)
        }
    }

    /**
     * Generate chat response with mentor persona
     */
    suspend fun generateMentorResponse(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        userStats: UserStats?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiConfig.getApiKey(GEMINI_PROVIDER)
                ?: return@withContext Result.failure(Exception("Gemini API key not configured"))

            val prompt = buildMentorPrompt(userMessage, conversationHistory, userStats)
            val response = callGeminiApi(apiKey, prompt)
            
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating mentor response", e)
            Result.failure(e)
        }
    }

    private suspend fun callGeminiApi(apiKey: String, prompt: String): String {
        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 1024
            )
        )

        val response = apiConfig.geminiApiService.generateContent(apiKey, request)
        
        if (!response.isSuccessful) {
            throw Exception("API call failed: ${response.code()} - ${response.message()}")
        }

        val body = response.body()
            ?: throw Exception("Empty response body")

        return body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No content in response")
    }

    private fun buildPerformanceAnalysisPrompt(userStats: UserStats, testAttempts: List<TestAttempt>): String {
        val accuracy = if (userStats.questionsAnswered > 0) {
            (userStats.correctAnswers.toFloat() / userStats.questionsAnswered * 100).toInt()
        } else 0

        val subjectStats = userStats.subjectPerformance.entries.joinToString("\n") { (subject, perf) ->
            "- $subject: ${perf.questionsAttempted} questions, ${(perf.accuracy * 100).toInt()}% accuracy"
        }

        return """
Analyze the following UPSC aspirant's performance data and provide insights in JSON format.

Performance Data:
- Total Questions Attempted: ${userStats.questionsAnswered}
- Correct Answers: ${userStats.correctAnswers}
- Overall Accuracy: $accuracy%
- Current Streak: ${userStats.currentStreak} days
- Tests Completed: ${testAttempts.size}

Subject-wise Performance:
$subjectStats

Provide your analysis in the following JSON format:
{
    "title": "Brief title for the analysis",
    "summary": "2-3 sentence summary of overall performance",
    "weakAreas": [
        {"subject": "Subject Name", "topic": "Optional topic", "accuracy": 0.0, "questionsAttempted": 0}
    ],
    "suggestions": [
        {"area": "Area to improve", "suggestion": "Specific actionable suggestion", "priority": "HIGH/MEDIUM/LOW"}
    ],
    "motivationalNote": "Short encouraging message"
}

Focus on being constructive and actionable. Identify the 2-3 weakest areas and provide specific improvement strategies.
""".trimIndent()
    }

    private fun buildQuickInsightPrompt(userStats: UserStats): String {
        val accuracy = if (userStats.questionsAnswered > 0) {
            (userStats.correctAnswers.toFloat() / userStats.questionsAnswered * 100).toInt()
        } else 0

        val weakestSubject = userStats.subjectPerformance.minByOrNull { it.value.accuracy }?.key

        return """
Generate a quick, encouraging insight for a UPSC aspirant. Be concise (1-2 sentences).

Stats:
- Accuracy: $accuracy%
- Questions Attempted: ${userStats.questionsAnswered}
- Current Streak: ${userStats.currentStreak} days
- Weakest Subject: ${weakestSubject ?: "None identified yet"}

Respond in JSON format:
{
    "message": "Brief motivational insight or tip",
    "actionPrompt": "Short call to action",
    "focusArea": "Subject/topic to focus on, or null"
}
""".trimIndent()
    }

    private fun buildTestResultPrompt(
        testName: String,
        score: Float,
        totalQuestions: Int,
        correctAnswers: Int,
        subjectBreakdown: Map<String, Pair<Int, Int>>
    ): String {
        val percentage = (score * 100).toInt()
        val breakdownStr = subjectBreakdown.entries.joinToString("\n") { (subject, stats) ->
            "- $subject: ${stats.first}/${stats.second} correct"
        }

        return """
Analyze this test result and provide feedback in JSON format.

Test: $testName
Score: $correctAnswers/$totalQuestions ($percentage%)

Subject Breakdown:
$breakdownStr

Respond in JSON format:
{
    "overallFeedback": "2-3 sentence overall assessment",
    "strengths": ["strength1", "strength2"],
    "areasToImprove": ["area1", "area2"],
    "nextSteps": "Specific next action to take",
    "encouragement": "Short encouraging message"
}
""".trimIndent()
    }

    private fun buildMentorPrompt(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        userStats: UserStats?
    ): String {
        val contextStr = if (userStats != null && userStats.questionsAnswered > 0) {
            val accuracy = (userStats.correctAnswers.toFloat() / userStats.questionsAnswered * 100).toInt()
            """
Student Context:
- Questions Attempted: ${userStats.questionsAnswered}
- Accuracy: $accuracy%
- Current Streak: ${userStats.currentStreak} days
"""
        } else {
            "Student Context: New user, no practice data yet."
        }

        val historyStr = conversationHistory.takeLast(6).joinToString("\n") { msg ->
            if (msg.isFromUser) "Student: ${msg.content}" else "Mentor: ${msg.content}"
        }

        return """
${MentorPersona.DEFAULT.systemPrompt}

$contextStr

Conversation History:
$historyStr

Student: $userMessage

Respond as the UPSC Mentor. Keep your response helpful, encouraging, and actionable. Use emojis sparingly for warmth.
""".trimIndent()
    }

    private fun parsePerformanceInsight(response: String): Result<PerformanceInsight> {
        return try {
            val jsonStr = extractJson(response)
            val json = JSONObject(jsonStr)

            val weakAreas = mutableListOf<WeakArea>()
            val weakAreasArray = json.optJSONArray("weakAreas") ?: JSONArray()
            for (i in 0 until weakAreasArray.length()) {
                val area = weakAreasArray.getJSONObject(i)
                weakAreas.add(
                    WeakArea(
                        subject = area.getString("subject"),
                        topic = area.optString("topic").takeIf { it.isNotEmpty() },
                        accuracy = area.optDouble("accuracy", 0.0).toFloat(),
                        questionsAttempted = area.optInt("questionsAttempted", 0)
                    )
                )
            }

            val suggestions = mutableListOf<ImprovementSuggestion>()
            val suggestionsArray = json.optJSONArray("suggestions") ?: JSONArray()
            for (i in 0 until suggestionsArray.length()) {
                val sugg = suggestionsArray.getJSONObject(i)
                suggestions.add(
                    ImprovementSuggestion(
                        area = sugg.getString("area"),
                        suggestion = sugg.getString("suggestion"),
                        priority = try {
                            SuggestionPriority.valueOf(sugg.optString("priority", "MEDIUM"))
                        } catch (e: Exception) {
                            SuggestionPriority.MEDIUM
                        }
                    )
                )
            }

            Result.success(
                PerformanceInsight(
                    title = json.optString("title", "Performance Analysis"),
                    summary = json.optString("summary", ""),
                    weakAreas = weakAreas,
                    suggestions = suggestions,
                    motivationalNote = json.optString("motivationalNote", "Keep practicing!")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing performance insight", e)
            Result.failure(e)
        }
    }

    private fun parseQuickInsight(response: String): Result<QuickInsight> {
        return try {
            val jsonStr = extractJson(response)
            val json = JSONObject(jsonStr)

            Result.success(
                QuickInsight(
                    message = json.optString("message", "Keep practicing!"),
                    actionPrompt = json.optString("actionPrompt", "Take a practice test"),
                    focusArea = json.optString("focusArea").takeIf { it.isNotEmpty() && it != "null" }
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing quick insight", e)
            Result.failure(e)
        }
    }

    private fun parseTestResultInsight(response: String): Result<TestResultInsight> {
        return try {
            val jsonStr = extractJson(response)
            val json = JSONObject(jsonStr)

            val strengths = mutableListOf<String>()
            val strengthsArray = json.optJSONArray("strengths") ?: JSONArray()
            for (i in 0 until strengthsArray.length()) {
                strengths.add(strengthsArray.getString(i))
            }

            val areasToImprove = mutableListOf<String>()
            val areasArray = json.optJSONArray("areasToImprove") ?: JSONArray()
            for (i in 0 until areasArray.length()) {
                areasToImprove.add(areasArray.getString(i))
            }

            Result.success(
                TestResultInsight(
                    overallFeedback = json.optString("overallFeedback", ""),
                    strengths = strengths,
                    areasToImprove = areasToImprove,
                    nextSteps = json.optString("nextSteps", ""),
                    encouragement = json.optString("encouragement", "Great effort!")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing test result insight", e)
            Result.failure(e)
        }
    }

    private fun extractJson(response: String): String {
        // Try to extract JSON from response (it might be wrapped in markdown code blocks)
        val jsonPattern = """\{[\s\S]*\}""".toRegex()
        return jsonPattern.find(response)?.value ?: response
    }

    private fun createEmptyInsight(): PerformanceInsight {
        return PerformanceInsight(
            title = "Start Your Journey!",
            summary = "Complete some practice tests to get personalized performance insights.",
            weakAreas = emptyList(),
            suggestions = listOf(
                ImprovementSuggestion(
                    area = "Getting Started",
                    suggestion = "Take your first practice test to begin tracking your progress.",
                    priority = SuggestionPriority.HIGH
                )
            ),
            motivationalNote = "Every expert was once a beginner. Start your UPSC journey today! 🚀"
        )
    }
}
