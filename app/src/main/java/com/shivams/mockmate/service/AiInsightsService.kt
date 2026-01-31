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
     * Generate a complete test with AI
     */
    suspend fun generateTest(
        topic: String,
        subject: String,
        difficulty: TestDifficulty,
        numberOfQuestions: Int,
        timeLimit: Int,
        negativeMarking: Boolean,
        negativeMarkingValue: Float
    ): Result<MockTest> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiConfig.getApiKey(GEMINI_PROVIDER)
                ?: return@withContext Result.failure(Exception("Gemini API key not configured. Please configure it in Settings."))

            val prompt = buildTestGenerationPrompt(
                topic, subject, difficulty, numberOfQuestions, negativeMarking, negativeMarkingValue
            )
            val response = callGeminiApi(apiKey, prompt)
            
            parseGeneratedTest(response, topic, subject, difficulty, timeLimit, negativeMarking, negativeMarkingValue)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating test", e)
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

    /**
     * Generate True-False statements for UPSC aptitude training
     */
    suspend fun generateTrueFalseStatements(
        topic: String,
        subject: String,
        difficulty: TestDifficulty,
        numberOfStatements: Int
    ): Result<List<TrueFalseStatement>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiConfig.getApiKey(GEMINI_PROVIDER)
                ?: return@withContext Result.failure(Exception("Gemini API key not configured. Please configure it in Settings."))

            val prompt = buildTrueFalsePrompt(topic, subject, difficulty, numberOfStatements)
            val response = callGeminiApi(apiKey, prompt)
            
            parseTrueFalseStatements(response, topic, subject, difficulty)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating True-False statements", e)
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
                maxOutputTokens = 8192
            )
        )

        Log.d(TAG, "Making Gemini API call...")
        val response = apiConfig.geminiApiService.generateContent(apiKey, request)
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "No error body"
            Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
            Log.e(TAG, "Error body: $errorBody")
            throw Exception("API call failed: ${response.code()} - ${response.message()} - $errorBody")
        }

        val body = response.body()
            ?: throw Exception("Empty response body")

        Log.d(TAG, "Gemini API call successful")
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

RESPONSE GUIDELINES:
- Keep responses CONCISE: under 50 words for intro/simple queries
- Use **bold** for metrics, data points, and key terms
- Use bullet points (•) for lists
- FOCUS on numbers, trends, and facts. Avoid fluff.
- Be direct, precise, and analytical. No sugarcoating.
- **At the end of your response, provide exactly 2 short follow-up questions** that the student might want to ask next, labeled exactly as:
[SUGGESTION] Question 1?
[SUGGESTION] Question 2?
These suggestions should be relevant to the context and guide the user deeper into the topic.
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
        val startIndex = response.indexOf('{')
        val endIndex = response.lastIndexOf('}')
        
        if (startIndex != -1 && endIndex != -1 && startIndex <= endIndex) {
            return response.substring(startIndex, endIndex + 1)
        }
        
        // Fallback: try finding first/last brackets anyway if strict finding failed
        return response.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
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

    private fun buildTestGenerationPrompt(
        topic: String,
        subject: String,
        difficulty: TestDifficulty,
        numberOfQuestions: Int,
        negativeMarking: Boolean,
        negativeMarkingValue: Float
    ): String {
        return """
Generate a UPSC-style multiple choice question test on the following topic. Create exactly $numberOfQuestions questions.

Topic: $topic
Subject: $subject
Difficulty: ${difficulty.name}
Negative Marking: ${if (negativeMarking) "Yes (-$negativeMarkingValue per wrong answer)" else "No"}

IMPORTANT: Respond ONLY with valid JSON, no explanations. Each question must have exactly 4 options with one correct answer.

JSON Format:
{
    "questions": [
        {
            "text": "Question text here?",
            "options": ["Option A", "Option B", "Option C", "Option D"],
            "correctOptionIndex": 0,
            "explanation": "Brief 1-2 sentence explanation",
            "topic": "$topic"
        }
    ]
}

Requirements:
- Questions should be factually accurate and exam-relevant
- Mix conceptual and factual questions
- Explanations should be clear and under 50 words
- Difficulty should match: ${difficulty.name}
- All questions must relate to: $topic under $subject
""".trimIndent()
    }

    private fun parseGeneratedTest(
        response: String,
        topic: String,
        subject: String,
        difficulty: TestDifficulty,
        timeLimit: Int,
        negativeMarking: Boolean,
        negativeMarkingValue: Float
    ): Result<MockTest> {
        return try {
            val jsonStr = extractJson(response)
            val json = JSONObject(jsonStr)
            
            val questionsArray = json.getJSONArray("questions")
            val questions = mutableListOf<Question>()
            
            for (i in 0 until questionsArray.length()) {
                val q = questionsArray.getJSONObject(i)
                val options = mutableListOf<String>()
                val optionsArray = q.getJSONArray("options")
                for (j in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(j))
                }
                
                questions.add(
                    Question(
                        text = q.getString("text"),
                        options = options,
                        correctOptionIndex = q.getInt("correctOptionIndex"),
                        explanation = q.optString("explanation", ""),
                        difficulty = when (difficulty) {
                            TestDifficulty.EASY -> QuestionDifficulty.EASY
                            TestDifficulty.MEDIUM -> QuestionDifficulty.MEDIUM
                            TestDifficulty.HARD -> QuestionDifficulty.HARD
                        },
                        subject = subject,
                        topic = q.optString("topic", topic)
                    )
                )
            }
            
            if (questions.isEmpty()) {
                return Result.failure(Exception("No questions generated"))
            }
            
            val test = MockTest(
                name = "$topic Test",
                difficulty = difficulty,
                questions = questions,
                timeLimit = timeLimit,
                negativeMarking = negativeMarking,
                negativeMarkingValue = negativeMarkingValue
            )
            
            Result.success(test)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing generated test", e)
            Result.failure(Exception("Failed to parse AI response: ${e.message}"))
        }
    }

    private fun buildTrueFalsePrompt(
        topic: String,
        subject: String,
        difficulty: TestDifficulty,
        numberOfStatements: Int
    ): String {
        val difficultyGuide = when (difficulty) {
            TestDifficulty.EASY -> "straightforward statements with obvious traps"
            TestDifficulty.MEDIUM -> "statements with subtle qualifiers and common misconceptions"
            TestDifficulty.HARD -> "highly deceptive statements with nuanced traps (UPSC Prelims level)"
        }

        return """
Generate exactly $numberOfStatements UPSC-style True/False statements about "$topic" in $subject.

CRITICAL REQUIREMENTS:
1. Each statement must be DEFINITIVELY True or False (no ambiguity)
2. Include a mix of True (~50%) and False (~50%) statements
3. False statements should be DECEPTIVELY crafted using these UPSC techniques:
   - Hidden qualifiers: "only", "always", "never", "exclusively", "entirely", "must"
   - Scope distortion: stating part as whole, or exception as general rule
   - Conceptual mismatch: correct concept in wrong context
   - Half-truths: partially correct statements that fail on specifics

Difficulty Level: ${difficulty.name} ($difficultyGuide)

IMPORTANT: Respond ONLY with valid JSON. No explanations outside JSON.

JSON Format:
{
    "statements": [
        {
            "statement": "The statement text here",
            "isTrue": true,
            "explanation": "Why this is true/false (2-3 sentences, very specific)",
            "trapWords": ["word1", "word2"],
            "upscTip": "One-liner rule for similar questions"
        }
    ]
}

Requirements:
- Statements must be factually accurate (verifiable from NCERT/standard sources)
- Focus on $subject facts related to $topic
- Explanations must clearly show WHY the statement is true/false
- trapWords array should contain the misleading words in false statements (empty for true statements)
- upscTip should be a memorable one-liner rule
""".trimIndent()
    }

    private fun parseTrueFalseStatements(
        response: String,
        topic: String,
        subject: String,
        difficulty: TestDifficulty
    ): Result<List<TrueFalseStatement>> {
        return try {
            val jsonStr = extractJson(response)
            val json = JSONObject(jsonStr)
            
            val statementsArray = json.getJSONArray("statements")
            val statements = mutableListOf<TrueFalseStatement>()
            
            for (i in 0 until statementsArray.length()) {
                val s = statementsArray.getJSONObject(i)
                
                val trapWords = mutableListOf<String>()
                val trapWordsArray = s.optJSONArray("trapWords") ?: JSONArray()
                for (j in 0 until trapWordsArray.length()) {
                    trapWords.add(trapWordsArray.getString(j))
                }
                
                statements.add(
                    TrueFalseStatement(
                        statement = s.getString("statement"),
                        isTrue = s.getBoolean("isTrue"),
                        explanation = s.optString("explanation", ""),
                        trapWords = trapWords,
                        upscTip = s.optString("upscTip", ""),
                        difficulty = when (difficulty) {
                            TestDifficulty.EASY -> QuestionDifficulty.EASY
                            TestDifficulty.MEDIUM -> QuestionDifficulty.MEDIUM
                            TestDifficulty.HARD -> QuestionDifficulty.HARD
                        },
                        subject = subject,
                        topic = topic
                    )
                )
            }
            
            if (statements.isEmpty()) {
                return Result.failure(Exception("No statements generated"))
            }
            
            Log.d(TAG, "Parsed ${statements.size} True-False statements")
            Result.success(statements)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing True-False statements", e)
            Result.failure(Exception("Failed to parse AI response: ${e.message}"))
        }
    }
    /**
     * Generate True-False statements from raw text (Prompt Based Import)
     */
    suspend fun generateTrueFalseStatementsFromText(
        text: String,
        numberOfStatements: Int
    ): Result<List<TrueFalseStatement>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiConfig.getApiKey(GEMINI_PROVIDER)
                ?: return@withContext Result.failure(Exception("Gemini API key not configured. Please configure it in Settings."))

            // Limit text length to avoid token limits (arbitrary safe limit ~3000 words)
            val truncatedText = if (text.length > 15000) text.take(15000) + "..." else text
            
            val prompt = buildTrueFalseFromTextPrompt(truncatedText, numberOfStatements)
            val response = callGeminiApi(apiKey, prompt)
            
            // Re-use existing parser, treating topic as "Custom" and subject as "Text Import"
            parseTrueFalseStatements(response, topic = "Imported Text", subject = "Custom", difficulty = TestDifficulty.MEDIUM)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating True-False statements from text", e)
            Result.failure(e)
        }
    }

    private fun buildTrueFalseFromTextPrompt(
        text: String,
        numberOfStatements: Int
    ): String {
        return """
Generate exactly $numberOfStatements UPSC-style True/False statements based ONLY on the provided text below.

CRITICAL REQUIREMENTS:
1. All statements must be derived directly from the provided text.
2. Include a mix of True (~50%) and False (~50%) statements.
3. False statements should optionally use UPSC deceptive techniques (hidden qualifiers, scope distortion) if the text allows.
4. If the text is short, generate as many high-quality statements as possible up to $numberOfStatements.

SOURCE TEXT:
$text

IMPORTANT: Respond ONLY with valid JSON. No explanations outside JSON.

JSON Format:
{
    "statements": [
        {
            "statement": "The statement text here",
            "isTrue": true,
            "explanation": "Why this is true/false based on the text (cite specific part)",
            "trapWords": ["word1"],
            "upscTip": "General rule derived from this fact"
        }
    ]
}
""".trimIndent()
    }
}
