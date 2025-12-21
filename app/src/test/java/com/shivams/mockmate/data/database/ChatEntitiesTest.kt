package com.shivams.mockmate.data.database

import com.shivams.mockmate.model.MessageType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Chat database entities
 */
class ChatEntitiesTest {

    @Test
    fun `ChatMessageEntity creation with all fields`() {
        val entity = ChatMessageEntity(
            id = "msg-123",
            content = "Hello, this is a test message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.TEXT.name
        )
        
        assertEquals("msg-123", entity.id)
        assertEquals("Hello, this is a test message", entity.content)
        assertTrue(entity.isFromUser)
        assertEquals("TEXT", entity.messageType)
    }

    @Test
    fun `ChatMessageEntity default messageType is TEXT`() {
        val entity = ChatMessageEntity(
            id = "msg-456",
            content = "Default type message",
            isFromUser = false,
            timestamp = System.currentTimeMillis()
        )
        
        assertEquals(MessageType.TEXT.name, entity.messageType)
    }

    @Test
    fun `ChatMessageEntity with different message types`() {
        val greetingEntity = ChatMessageEntity(
            id = "msg-1",
            content = "Welcome!",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.GREETING.name
        )
        
        val insightEntity = ChatMessageEntity(
            id = "msg-2",
            content = "Insight data",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.INSIGHT_CARD.name
        )
        
        assertEquals("GREETING", greetingEntity.messageType)
        assertEquals("INSIGHT_CARD", insightEntity.messageType)
    }

    @Test
    fun `ChatSessionEntity creation with defaults`() {
        val session = ChatSessionEntity()
        
        assertEquals("default_session", session.id)
        assertTrue(session.lastUpdated > 0)
        assertEquals(0, session.messageCount)
    }

    @Test
    fun `ChatSessionEntity creation with custom values`() {
        val customTime = 1703145600000L // Some fixed timestamp
        val session = ChatSessionEntity(
            id = "custom_session",
            lastUpdated = customTime,
            messageCount = 10
        )
        
        assertEquals("custom_session", session.id)
        assertEquals(customTime, session.lastUpdated)
        assertEquals(10, session.messageCount)
    }

    @Test
    fun `ChatMessageEntity timestamp is valid`() {
        val beforeCreation = System.currentTimeMillis()
        val entity = ChatMessageEntity(
            id = "msg-time-test",
            content = "Timestamp test",
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        val afterCreation = System.currentTimeMillis()
        
        assertTrue(entity.timestamp >= beforeCreation)
        assertTrue(entity.timestamp <= afterCreation)
    }

    @Test
    fun `MessageType enum to string conversion`() {
        assertEquals("TEXT", MessageType.TEXT.name)
        assertEquals("GREETING", MessageType.GREETING.name)
        assertEquals("INSIGHT_CARD", MessageType.INSIGHT_CARD.name)
        assertEquals("SUGGESTION", MessageType.SUGGESTION.name)
    }

    @Test
    fun `MessageType string to enum conversion`() {
        assertEquals(MessageType.TEXT, MessageType.valueOf("TEXT"))
        assertEquals(MessageType.GREETING, MessageType.valueOf("GREETING"))
        assertEquals(MessageType.INSIGHT_CARD, MessageType.valueOf("INSIGHT_CARD"))
        assertEquals(MessageType.SUGGESTION, MessageType.valueOf("SUGGESTION"))
    }

    @Test
    fun `ChatMessageEntity user vs mentor messages`() {
        val userMessage = ChatMessageEntity(
            id = "user-msg",
            content = "User question",
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        
        val mentorMessage = ChatMessageEntity(
            id = "mentor-msg",
            content = "Mentor response",
            isFromUser = false,
            timestamp = System.currentTimeMillis()
        )
        
        assertTrue(userMessage.isFromUser)
        assertFalse(mentorMessage.isFromUser)
    }
}
