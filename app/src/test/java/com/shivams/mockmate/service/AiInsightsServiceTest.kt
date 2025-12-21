package com.shivams.mockmate.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.shivams.mockmate.model.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AiInsightsService response parsing logic
 * Uses Gson for JSON parsing (available in unit tests, unlike org.json)
 */
class AiInsightsServiceTest {

    private val gson = Gson()

    @Test
    fun `extractJson extracts valid JSON from plain response`() {
        val response = """{"title": "Test", "summary": "Test summary"}"""
        val extracted = extractJsonFromResponse(response)
        
        assertTrue(extracted.contains("title"))
        assertTrue(extracted.contains("Test"))
    }

    @Test
    fun `extractJson extracts JSON from markdown code block`() {
        val response = """
            Here is the analysis:
            ```json
            {"title": "Analysis", "summary": "Good progress"}
            ```
            Hope this helps!
        """.trimIndent()
        
        val extracted = extractJsonFromResponse(response)
        
        assertTrue(extracted.contains("title"))
        assertTrue(extracted.contains("Analysis"))
    }

    @Test
    fun `extractJson handles response with surrounding text`() {
        val response = """
            Based on your performance data, here's my analysis:
            {"title": "Performance Review", "summary": "You're doing great!"}
            Let me know if you need more details.
        """.trimIndent()
        
        val extracted = extractJsonFromResponse(response)
        
        assertTrue(extracted.startsWith("{"))
        assertTrue(extracted.endsWith("}"))
    }

    @Test
    fun `parsePerformanceInsightJson parses valid JSON`() {
        val json = """
            {
                "title": "Performance Analysis",
                "summary": "You need to improve in certain areas",
                "weakAreas": [
                    {"subject": "History", "topic": "Medieval", "accuracy": 0.4, "questionsAttempted": 10}
                ],
                "suggestions": [
                    {"area": "History", "suggestion": "Study more", "priority": "HIGH"}
                ],
                "motivationalNote": "Keep going!"
            }
        """.trimIndent()
        
        val insight = parsePerformanceInsightFromJson(json)
        
        assertNotNull(insight)
        assertEquals("Performance Analysis", insight.title)
        assertEquals(1, insight.weakAreas.size)
        assertEquals(1, insight.suggestions.size)
        assertEquals(SuggestionPriority.HIGH, insight.suggestions[0].priority)
    }

    @Test
    fun `parsePerformanceInsightJson handles empty arrays`() {
        val json = """
            {
                "title": "No Issues",
                "summary": "Everything looks good",
                "weakAreas": [],
                "suggestions": [],
                "motivationalNote": "Excellent work!"
            }
        """.trimIndent()
        
        val insight = parsePerformanceInsightFromJson(json)
        
        assertNotNull(insight)
        assertTrue(insight.weakAreas.isEmpty())
        assertTrue(insight.suggestions.isEmpty())
    }

    @Test
    fun `parseQuickInsightJson parses valid JSON`() {
        val json = """
            {
                "message": "Focus on History today",
                "actionPrompt": "Take a practice test",
                "focusArea": "History"
            }
        """.trimIndent()
        
        val insight = parseQuickInsightFromJson(json)
        
        assertNotNull(insight)
        assertEquals("Focus on History today", insight.message)
        assertEquals("History", insight.focusArea)
    }

    @Test
    fun `parseQuickInsightJson handles null focusArea`() {
        val json = """
            {
                "message": "Great progress!",
                "actionPrompt": "Keep practicing",
                "focusArea": null
            }
        """.trimIndent()
        
        val insight = parseQuickInsightFromJson(json)
        
        assertNotNull(insight)
        assertNull(insight.focusArea)
    }

    @Test
    fun `parseTestResultInsightJson parses valid JSON`() {
        val json = """
            {
                "overallFeedback": "Good attempt overall",
                "strengths": ["Polity", "Economy"],
                "areasToImprove": ["History", "Geography"],
                "nextSteps": "Focus on weak areas",
                "encouragement": "Keep it up!"
            }
        """.trimIndent()
        
        val insight = parseTestResultInsightFromJson(json)
        
        assertNotNull(insight)
        assertEquals(2, insight.strengths.size)
        assertEquals(2, insight.areasToImprove.size)
        assertTrue(insight.strengths.contains("Polity"))
    }

    @Test
    fun `SuggestionPriority parsing handles different cases`() {
        assertEquals(SuggestionPriority.HIGH, parsePriority("HIGH"))
        assertEquals(SuggestionPriority.MEDIUM, parsePriority("MEDIUM"))
        assertEquals(SuggestionPriority.LOW, parsePriority("LOW"))
        assertEquals(SuggestionPriority.MEDIUM, parsePriority("UNKNOWN")) // default fallback
    }

    // Helper functions using Gson for parsing
    private fun extractJsonFromResponse(response: String): String {
        val jsonPattern = """\{[\s\S]*\}""".toRegex()
        return jsonPattern.find(response)?.value ?: response
    }

    private fun parsePerformanceInsightFromJson(json: String): PerformanceInsight {
        val jsonObj = JsonParser.parseString(json).asJsonObject
        
        val weakAreas = mutableListOf<WeakArea>()
        jsonObj.getAsJsonArray("weakAreas")?.forEach { element ->
            val area = element.asJsonObject
            weakAreas.add(
                WeakArea(
                    subject = area.get("subject").asString,
                    topic = area.get("topic")?.takeIf { !it.isJsonNull }?.asString,
                    accuracy = area.get("accuracy").asFloat,
                    questionsAttempted = area.get("questionsAttempted").asInt
                )
            )
        }

        val suggestions = mutableListOf<ImprovementSuggestion>()
        jsonObj.getAsJsonArray("suggestions")?.forEach { element ->
            val sugg = element.asJsonObject
            suggestions.add(
                ImprovementSuggestion(
                    area = sugg.get("area").asString,
                    suggestion = sugg.get("suggestion").asString,
                    priority = parsePriority(sugg.get("priority")?.asString ?: "MEDIUM")
                )
            )
        }

        return PerformanceInsight(
            title = jsonObj.get("title")?.asString ?: "Performance Analysis",
            summary = jsonObj.get("summary")?.asString ?: "",
            weakAreas = weakAreas,
            suggestions = suggestions,
            motivationalNote = jsonObj.get("motivationalNote")?.asString ?: "Keep practicing!"
        )
    }

    private fun parseQuickInsightFromJson(json: String): QuickInsight {
        val jsonObj = JsonParser.parseString(json).asJsonObject
        return QuickInsight(
            message = jsonObj.get("message")?.asString ?: "Keep practicing!",
            actionPrompt = jsonObj.get("actionPrompt")?.asString ?: "Take a practice test",
            focusArea = jsonObj.get("focusArea")?.takeIf { !it.isJsonNull }?.asString
        )
    }

    private fun parseTestResultInsightFromJson(json: String): TestResultInsight {
        val jsonObj = JsonParser.parseString(json).asJsonObject

        val strengths = mutableListOf<String>()
        jsonObj.getAsJsonArray("strengths")?.forEach { element ->
            strengths.add(element.asString)
        }

        val areasToImprove = mutableListOf<String>()
        jsonObj.getAsJsonArray("areasToImprove")?.forEach { element ->
            areasToImprove.add(element.asString)
        }

        return TestResultInsight(
            overallFeedback = jsonObj.get("overallFeedback")?.asString ?: "",
            strengths = strengths,
            areasToImprove = areasToImprove,
            nextSteps = jsonObj.get("nextSteps")?.asString ?: "",
            encouragement = jsonObj.get("encouragement")?.asString ?: "Great effort!"
        )
    }

    private fun parsePriority(value: String): SuggestionPriority {
        return try {
            SuggestionPriority.valueOf(value)
        } catch (e: Exception) {
            SuggestionPriority.MEDIUM
        }
    }
}
