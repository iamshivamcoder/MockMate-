package com.shivams.mockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.repositories.MentorChatRepository
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.ChatMessage
import com.shivams.mockmate.model.ChatUiState
import com.shivams.mockmate.model.MessageType
import com.shivams.mockmate.model.QuickAction
import com.shivams.mockmate.service.AiInsightsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Mentor Chat screen
 */
class MentorChatViewModel(
    private val chatRepository: MentorChatRepository,
    private val aiInsightsService: AiInsightsService,
    private val testRepository: TestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val quickActions = QuickAction.DEFAULT_ACTIONS

    init {
        initializeChat()
        observeMessages()
    }

    private fun initializeChat() {
        viewModelScope.launch {
            chatRepository.initializeChatIfEmpty()
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.getAllMessages().collect { messages ->
                val suggestions = generateContextSuggestions(messages)
                _uiState.update { it.copy(messages = messages, contextSuggestions = suggestions) }
            }
        }
    }

    /**
     * Generate context-aware suggestions based on recent conversation
     */
    private fun generateContextSuggestions(messages: List<ChatMessage>): List<String> {
        if (messages.isEmpty()) return emptyList()
        
        val lastMentorMessage = messages.lastOrNull { !it.isFromUser }?.content ?: return emptyList()
        val lastUserMessage = messages.lastOrNull { it.isFromUser }?.content ?: ""
        
        // Generate relevant follow-up questions based on conversation context
        return when {
            lastMentorMessage.contains("performance", ignoreCase = true) ||
            lastMentorMessage.contains("accuracy", ignoreCase = true) ||
            lastMentorMessage.contains("score", ignoreCase = true) -> listOf(
                "How can I improve this?",
                "What topics should I prioritize?"
            )
            lastMentorMessage.contains("study", ignoreCase = true) ||
            lastMentorMessage.contains("revision", ignoreCase = true) -> listOf(
                "Create a weekly plan for me",
                "Suggest effective revision techniques"
            )
            lastMentorMessage.contains("topic", ignoreCase = true) ||
            lastMentorMessage.contains("syllabus", ignoreCase = true) -> listOf(
                "Which topics are most important for Prelims?",
                "Give me resources for this topic"
            )
            lastMentorMessage.contains("time", ignoreCase = true) ||
            lastMentorMessage.contains("schedule", ignoreCase = true) -> listOf(
                "How many hours should I study daily?",
                "When should I start answer writing practice?"
            )
            lastMentorMessage.contains("weak", ignoreCase = true) ||
            lastMentorMessage.contains("improve", ignoreCase = true) -> listOf(
                "How do I overcome this weakness?",
                "Suggest practice resources"
            )
            lastUserMessage.contains("prelims", ignoreCase = true) -> listOf(
                "What is the ideal Prelims strategy?",
                "How many mock tests should I attempt?"
            )
            lastUserMessage.contains("mains", ignoreCase = true) -> listOf(
                "Tips for answer writing",
                "How to structure my answers?"
            )
            else -> listOf(
                "Analyze my weak topics",
                "Give me a quick study tip"
            )
        }
    }

    /**
     * Update the input text field
     */
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * Send a message to the AI mentor
     */
    fun sendMessage(content: String = _uiState.value.inputText) {
        val message = content.trim()
        if (message.isEmpty()) return

        viewModelScope.launch {
            // Clear input and show typing indicator
            _uiState.update { 
                it.copy(
                    inputText = "",
                    isTyping = true,
                    error = null
                )
            }

            try {
                // Add user message
                chatRepository.addUserMessage(message)

                // Get conversation history for context
                val history = chatRepository.getRecentMessagesForContext(10)
                
                // Get user stats for context
                val userStats = try {
                    testRepository.userStats.first()
                } catch (e: Exception) {
                    null
                }

                // Generate AI response
                val result = aiInsightsService.generateMentorResponse(
                    userMessage = message,
                    conversationHistory = history,
                    userStats = userStats
                )

                result.fold(
                    onSuccess = { response ->
                        chatRepository.addMentorMessage(response, MessageType.TEXT)
                    },
                    onFailure = { error ->
                        val errorMessage = when {
                            error.message?.contains("API key") == true -> 
                                "Please configure your Gemini API key in Settings to use the AI Mentor."
                            error.message?.contains("network") == true ->
                                "Network error. Please check your internet connection."
                            else -> 
                                "Sorry, I couldn't process your request. Please try again."
                        }
                        chatRepository.addMentorMessage(errorMessage, MessageType.TEXT)
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "An error occurred. Please try again.")
                }
            } finally {
                _uiState.update { it.copy(isTyping = false) }
            }
        }
    }

    /**
     * Send a quick action prompt
     */
    fun sendQuickAction(action: QuickAction) {
        sendMessage(action.prompt)
    }

    /**
     * Clear chat history
     */
    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepository.clearHistory()
            _uiState.update { it.copy(error = null, contextSuggestions = emptyList()) }
        }
    }

    /**
     * Dismiss error message
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Request performance analysis
     */
    fun requestPerformanceAnalysis() {
        sendMessage("Please analyze my overall performance and tell me where I need to improve the most.")
    }
}
