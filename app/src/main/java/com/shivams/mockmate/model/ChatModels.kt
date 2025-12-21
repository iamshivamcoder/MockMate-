package com.shivams.mockmate.model

import java.util.Date
import java.util.UUID

/**
 * Data models for AI Mentor Chatbot
 */

/**
 * Represents a single chat message
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date = Date(),
    val messageType: MessageType = MessageType.TEXT
)

/**
 * Type of chat message
 */
enum class MessageType { 
    TEXT,           // Regular text message
    INSIGHT_CARD,   // Performance insight card
    SUGGESTION,     // Improvement suggestion
    GREETING        // Initial greeting message
}

/**
 * Mentor persona configuration
 */
data class MentorPersona(
    val name: String = "UPSC Mentor",
    val systemPrompt: String,
    val greeting: String,
    val encouragements: List<String>
) {
    companion object {
        val DEFAULT = MentorPersona(
            name = "Vimarsh",
            systemPrompt = """You are Vimarsh, a UPSC mentor defined by brutal clarity and data-driven insights.

Core Personality:
- Objective, precise, data-first
- Less emotional, more factual
- Brutally clear and result-centric

Your Mentor Style:
1. Talk in numbers, trends, and ROI (Return on Investment)
2. Use data comparisons (e.g., "Your accuracy is 20% below the average topper")
3. Focus on Heatmaps and Graph-based insights where possible
4. Example feedback: "Your score stagnation is due to option elimination failure", "Topic-wise ROI is low here"

Objective:
- Provide objective, number-driven guidance
- Do not sugarcoat failures; analyze them for root causes
- Be direct and precise""",
            greeting = "I am Vimarsh. I analyze performance with data, not emotions.\n\nUpload your test data or ask me to analyze your latest attempt. Let's look at the numbers.",
            encouragements = listOf(
                "Data shows consistency leads to improvement.",
                "Your accuracy trend is positive. Maintain this vector.",
                "Optimization of weak areas yields the highest ROI.",
                "Numbers don't lie. Trust the analysis.",
                "Refine your strategy based on these metrics."
            )
        )
    }
}

/**
 * Quick action chip for suggested prompts
 */
data class QuickAction(
    val label: String,
    val prompt: String,
    val icon: String? = null
) {
    companion object {
        val DEFAULT_ACTIONS = listOf(
            QuickAction("📊 Analyze Performance", "Analyze my overall performance and tell me where I need to improve."),
            QuickAction("📚 Study Tips", "Give me effective study tips for UPSC preparation."),
            QuickAction("🎯 Focus Areas", "Based on my performance, which topics should I focus on?"),
            QuickAction("💡 Quick Tip", "Give me a quick motivational tip to stay focused."),
            QuickAction("📖 Revision Strategy", "Suggest an effective revision strategy for me."),
            QuickAction("⏰ Time Management", "How should I manage my time for UPSC preparation?")
        )
    }
}

/**
 * Chat UI state
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
)
