package com.shivams.mockmate.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AI Models
 */
class AiModelsTest {

    @Test
    fun `PerformanceInsight creation with all fields`() {
        val weakAreas = listOf(
            WeakArea(
                subject = "History",
                topic = "Ancient India",
                accuracy = 0.45f,
                questionsAttempted = 20
            )
        )
        
        val suggestions = listOf(
            ImprovementSuggestion(
                area = "History",
                suggestion = "Focus on Ancient India chronology",
                priority = SuggestionPriority.HIGH
            )
        )
        
        val insight = PerformanceInsight(
            title = "Performance Analysis",
            summary = "You need to improve in History",
            weakAreas = weakAreas,
            suggestions = suggestions,
            motivationalNote = "Keep going!"
        )
        
        assertEquals("Performance Analysis", insight.title)
        assertEquals(1, insight.weakAreas.size)
        assertEquals(1, insight.suggestions.size)
        assertEquals(SuggestionPriority.HIGH, insight.suggestions[0].priority)
    }

    @Test
    fun `WeakArea accuracy bounds are valid`() {
        val weakArea = WeakArea(
            subject = "Geography",
            topic = null,
            accuracy = 0.3f,
            questionsAttempted = 15
        )
        
        assertTrue(weakArea.accuracy >= 0f)
        assertTrue(weakArea.accuracy <= 1f)
        assertNull(weakArea.topic)
    }

    @Test
    fun `SuggestionPriority enum values exist`() {
        val priorities = SuggestionPriority.values()
        assertEquals(3, priorities.size)
        assertTrue(priorities.contains(SuggestionPriority.HIGH))
        assertTrue(priorities.contains(SuggestionPriority.MEDIUM))
        assertTrue(priorities.contains(SuggestionPriority.LOW))
    }

    @Test
    fun `QuickInsight with null focusArea`() {
        val quickInsight = QuickInsight(
            message = "Great progress!",
            actionPrompt = "Take a test",
            focusArea = null
        )
        
        assertNull(quickInsight.focusArea)
        assertNotNull(quickInsight.message)
    }

    @Test
    fun `TestResultInsight with empty lists`() {
        val insight = TestResultInsight(
            overallFeedback = "Good attempt",
            strengths = emptyList(),
            areasToImprove = emptyList(),
            nextSteps = "Practice more",
            encouragement = "Well done!"
        )
        
        assertTrue(insight.strengths.isEmpty())
        assertTrue(insight.areasToImprove.isEmpty())
    }

    @Test
    fun `TestResultInsight with populated lists`() {
        val insight = TestResultInsight(
            overallFeedback = "Good attempt",
            strengths = listOf("Polity", "Economy"),
            areasToImprove = listOf("History", "Geography"),
            nextSteps = "Focus on weak areas",
            encouragement = "Keep it up!"
        )
        
        assertEquals(2, insight.strengths.size)
        assertEquals(2, insight.areasToImprove.size)
        assertTrue(insight.strengths.contains("Polity"))
        assertTrue(insight.areasToImprove.contains("History"))
    }
}
