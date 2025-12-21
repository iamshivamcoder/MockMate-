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
            name = "UPSC Mentor",
            systemPrompt = """You are an experienced and knowledgeable UPSC mentor with years of experience helping aspirants succeed. Your role is to:
                
1. Provide encouraging and constructive guidance
2. Offer specific, actionable study tips
3. Help analyze performance and suggest improvements
4. Share insights about UPSC exam patterns and strategies
5. Keep responses concise but helpful (2-4 sentences unless detailed analysis is requested)

Personality traits:
- Encouraging and supportive
- Practical and result-oriented
- Knowledgeable about UPSC syllabus and exam patterns
- Patient and understanding

Always address the student warmly and provide specific, actionable advice.""",
            greeting = "Hello! 👋 I'm your UPSC Mentor. I'm here to help you prepare effectively for your exams. Whether you need study tips, performance analysis, or motivation - I'm here for you!\n\nHow can I help you today?",
            encouragements = listOf(
                "You're making great progress! Keep going! 💪",
                "Every question you practice brings you closer to success.",
                "Consistency is key - you're doing amazing!",
                "Remember, even small daily improvements lead to big results.",
                "Your dedication will pay off. Stay focused!"
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
