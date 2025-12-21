package com.shivams.mockmate.model

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

/**
 * Unit tests for Chat Models
 */
class ChatModelsTest {

    @Test
    fun `ChatMessage creation with default values`() {
        val message = ChatMessage(
            content = "Hello, how can I help?",
            isFromUser = false
        )
        
        assertNotNull(message.id)
        assertEquals("Hello, how can I help?", message.content)
        assertFalse(message.isFromUser)
        assertEquals(MessageType.TEXT, message.messageType)
        assertNotNull(message.timestamp)
    }

    @Test
    fun `ChatMessage creation for user message`() {
        val message = ChatMessage(
            content = "I need help with History",
            isFromUser = true,
            messageType = MessageType.TEXT
        )
        
        assertTrue(message.isFromUser)
        assertEquals(MessageType.TEXT, message.messageType)
    }

    @Test
    fun `ChatMessage with different message types`() {
        val greetingMessage = ChatMessage(
            content = "Welcome!",
            isFromUser = false,
            messageType = MessageType.GREETING
        )
        
        val insightMessage = ChatMessage(
            content = "Performance insight data",
            isFromUser = false,
            messageType = MessageType.INSIGHT_CARD
        )
        
        assertEquals(MessageType.GREETING, greetingMessage.messageType)
        assertEquals(MessageType.INSIGHT_CARD, insightMessage.messageType)
    }

    @Test
    fun `MessageType enum values exist`() {
        val types = MessageType.values()
        assertEquals(4, types.size)
        assertTrue(types.contains(MessageType.TEXT))
        assertTrue(types.contains(MessageType.INSIGHT_CARD))
        assertTrue(types.contains(MessageType.SUGGESTION))
        assertTrue(types.contains(MessageType.GREETING))
    }

    @Test
    fun `MentorPersona default values are set`() {
        val persona = MentorPersona.DEFAULT
        
        assertEquals("UPSC Mentor", persona.name)
        assertNotNull(persona.systemPrompt)
        assertTrue(persona.systemPrompt.isNotEmpty())
        assertNotNull(persona.greeting)
        assertTrue(persona.greeting.isNotEmpty())
        assertTrue(persona.encouragements.isNotEmpty())
    }

    @Test
    fun `MentorPersona encouragements list is not empty`() {
        val encouragements = MentorPersona.DEFAULT.encouragements
        
        assertTrue(encouragements.size >= 3)
        encouragements.forEach { 
            assertTrue(it.isNotEmpty())
        }
    }

    @Test
    fun `QuickAction default actions exist`() {
        val actions = QuickAction.DEFAULT_ACTIONS
        
        assertTrue(actions.isNotEmpty())
        assertTrue(actions.size >= 4)
        
        // Verify each action has required fields
        actions.forEach { action ->
            assertTrue(action.label.isNotEmpty())
            assertTrue(action.prompt.isNotEmpty())
        }
    }

    @Test
    fun `QuickAction creation with custom values`() {
        val customAction = QuickAction(
            label = "Custom Action",
            prompt = "Do something custom",
            icon = "🎯"
        )
        
        assertEquals("Custom Action", customAction.label)
        assertEquals("Do something custom", customAction.prompt)
        assertEquals("🎯", customAction.icon)
    }

    @Test
    fun `ChatUiState default values`() {
        val state = ChatUiState()
        
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isTyping)
        assertNull(state.error)
        assertEquals("", state.inputText)
    }

    @Test
    fun `ChatUiState with custom values`() {
        val messages = listOf(
            ChatMessage(content = "Hi", isFromUser = true),
            ChatMessage(content = "Hello!", isFromUser = false)
        )
        
        val state = ChatUiState(
            messages = messages,
            isLoading = false,
            isTyping = true,
            error = null,
            inputText = "typing..."
        )
        
        assertEquals(2, state.messages.size)
        assertTrue(state.isTyping)
        assertEquals("typing...", state.inputText)
    }

    @Test
    fun `ChatUiState with error state`() {
        val state = ChatUiState(
            error = "Network error occurred"
        )
        
        assertNotNull(state.error)
        assertEquals("Network error occurred", state.error)
    }

    @Test
    fun `ChatMessage unique IDs are generated`() {
        val message1 = ChatMessage(content = "Test 1", isFromUser = true)
        val message2 = ChatMessage(content = "Test 2", isFromUser = true)
        
        assertNotEquals(message1.id, message2.id)
    }
}
