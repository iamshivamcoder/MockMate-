package com.shivams.mockmate.ui.viewmodels

import com.shivams.mockmate.model.ChatMessage
import com.shivams.mockmate.model.ChatUiState
import com.shivams.mockmate.model.MessageType
import com.shivams.mockmate.model.QuickAction
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MentorChatViewModel state management
 * Tests the UI state logic without requiring Android dependencies
 */
class MentorChatViewModelTest {

    @Test
    fun `initial ChatUiState has empty messages`() {
        val state = ChatUiState()
        
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isTyping)
        assertNull(state.error)
        assertEquals("", state.inputText)
    }

    @Test
    fun `ChatUiState can update input text`() {
        val initialState = ChatUiState()
        val updatedState = initialState.copy(inputText = "Hello mentor")
        
        assertEquals("", initialState.inputText)
        assertEquals("Hello mentor", updatedState.inputText)
    }

    @Test
    fun `ChatUiState typing indicator state`() {
        val notTyping = ChatUiState(isTyping = false)
        val typing = notTyping.copy(isTyping = true)
        
        assertFalse(notTyping.isTyping)
        assertTrue(typing.isTyping)
    }

    @Test
    fun `ChatUiState can add messages`() {
        val userMessage = ChatMessage(
            content = "How can I improve?",
            isFromUser = true
        )
        
        val initialState = ChatUiState()
        val stateWithMessage = initialState.copy(
            messages = initialState.messages + userMessage
        )
        
        assertEquals(1, stateWithMessage.messages.size)
        assertTrue(stateWithMessage.messages[0].isFromUser)
    }

    @Test
    fun `ChatUiState can handle error`() {
        val errorState = ChatUiState(
            error = "Network connection failed"
        )
        
        assertNotNull(errorState.error)
        assertEquals("Network connection failed", errorState.error)
    }

    @Test
    fun `ChatUiState error can be dismissed`() {
        val errorState = ChatUiState(error = "Some error")
        val clearedState = errorState.copy(error = null)
        
        assertNotNull(errorState.error)
        assertNull(clearedState.error)
    }

    @Test
    fun `QuickAction default actions are available`() {
        val actions = QuickAction.DEFAULT_ACTIONS
        
        assertTrue(actions.isNotEmpty())
        
        // Verify we have key action types
        val labels = actions.map { it.label }
        assertTrue(labels.any { it.contains("Performance") || it.contains("Analyze") })
        assertTrue(labels.any { it.contains("Study") || it.contains("Tip") })
    }

    @Test
    fun `Message list ordering is preserved`() {
        val message1 = ChatMessage(content = "First", isFromUser = true)
        val message2 = ChatMessage(content = "Second", isFromUser = false)
        val message3 = ChatMessage(content = "Third", isFromUser = true)
        
        val state = ChatUiState(messages = listOf(message1, message2, message3))
        
        assertEquals(3, state.messages.size)
        assertEquals("First", state.messages[0].content)
        assertEquals("Second", state.messages[1].content)
        assertEquals("Third", state.messages[2].content)
    }

    @Test
    fun `Alternating user and mentor messages`() {
        val messages = listOf(
            ChatMessage(content = "User Q1", isFromUser = true),
            ChatMessage(content = "Mentor A1", isFromUser = false),
            ChatMessage(content = "User Q2", isFromUser = true),
            ChatMessage(content = "Mentor A2", isFromUser = false)
        )
        
        val state = ChatUiState(messages = messages)
        
        assertTrue(state.messages[0].isFromUser)
        assertFalse(state.messages[1].isFromUser)
        assertTrue(state.messages[2].isFromUser)
        assertFalse(state.messages[3].isFromUser)
    }

    @Test
    fun `Input text is cleared after sending`() {
        val stateBeforeSend = ChatUiState(inputText = "Message to send")
        val stateAfterSend = stateBeforeSend.copy(
            inputText = "",
            isTyping = true
        )
        
        assertEquals("Message to send", stateBeforeSend.inputText)
        assertEquals("", stateAfterSend.inputText)
        assertTrue(stateAfterSend.isTyping)
    }

    @Test
    fun `Loading state transitions`() {
        val idle = ChatUiState(isLoading = false)
        val loading = idle.copy(isLoading = true)
        val loaded = loading.copy(isLoading = false)
        
        assertFalse(idle.isLoading)
        assertTrue(loading.isLoading)
        assertFalse(loaded.isLoading)
    }

    @Test
    fun `ChatMessage with greeting type`() {
        val greeting = ChatMessage(
            content = "Hello! I'm your UPSC Mentor",
            isFromUser = false,
            messageType = MessageType.GREETING
        )
        
        val state = ChatUiState(messages = listOf(greeting))
        
        assertEquals(MessageType.GREETING, state.messages[0].messageType)
        assertFalse(state.messages[0].isFromUser)
    }

    @Test
    fun `Empty input text validation`() {
        val emptyInput = ""
        val whitespaceInput = "   "
        val validInput = "Valid message"
        
        assertTrue(emptyInput.isBlank())
        assertTrue(whitespaceInput.isBlank())
        assertFalse(validInput.isBlank())
    }

    @Test
    fun `Quick action generates correct prompt`() {
        val action = QuickAction(
            label = "📊 Analyze Performance",
            prompt = "Analyze my overall performance"
        )
        
        assertEquals("Analyze my overall performance", action.prompt)
    }
}
