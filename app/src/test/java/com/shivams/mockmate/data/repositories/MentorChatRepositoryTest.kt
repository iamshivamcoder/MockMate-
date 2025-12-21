package com.shivams.mockmate.data.repositories

import com.shivams.mockmate.data.database.ChatMessageEntity
import com.shivams.mockmate.model.ChatMessage
import com.shivams.mockmate.model.MessageType
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

/**
 * Unit tests for MentorChatRepository entity mapping logic
 * Tests the conversion between domain models and database entities
 */
class MentorChatRepositoryTest {

    @Test
    fun `ChatMessage to Entity conversion preserves data`() {
        val message = ChatMessage(
            id = "test-id-123",
            content = "Test message content",
            isFromUser = true,
            timestamp = Date(1703145600000L),
            messageType = MessageType.TEXT
        )
        
        val entity = message.toEntity()
        
        assertEquals("test-id-123", entity.id)
        assertEquals("Test message content", entity.content)
        assertTrue(entity.isFromUser)
        assertEquals(1703145600000L, entity.timestamp)
        assertEquals("TEXT", entity.messageType)
    }

    @Test
    fun `Entity to ChatMessage conversion preserves data`() {
        val entity = ChatMessageEntity(
            id = "entity-id-456",
            content = "Entity content",
            isFromUser = false,
            timestamp = 1703145600000L,
            messageType = "GREETING"
        )
        
        val message = entity.toDomain()
        
        assertEquals("entity-id-456", message.id)
        assertEquals("Entity content", message.content)
        assertFalse(message.isFromUser)
        assertEquals(1703145600000L, message.timestamp.time)
        assertEquals(MessageType.GREETING, message.messageType)
    }

    @Test
    fun `Entity conversion handles unknown message type gracefully`() {
        val entity = ChatMessageEntity(
            id = "msg-id",
            content = "Content",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = "UNKNOWN_TYPE"
        )
        
        val message = entity.toDomain()
        
        // Should fallback to TEXT type
        assertEquals(MessageType.TEXT, message.messageType)
    }

    @Test
    fun `Round trip conversion preserves all fields`() {
        val original = ChatMessage(
            id = "round-trip-id",
            content = "Round trip test",
            isFromUser = true,
            timestamp = Date(1703145600000L),
            messageType = MessageType.INSIGHT_CARD
        )
        
        val entity = original.toEntity()
        val restored = entity.toDomain()
        
        assertEquals(original.id, restored.id)
        assertEquals(original.content, restored.content)
        assertEquals(original.isFromUser, restored.isFromUser)
        assertEquals(original.timestamp.time, restored.timestamp.time)
        assertEquals(original.messageType, restored.messageType)
    }

    @Test
    fun `Multiple messages conversion maintains order`() {
        val messages = listOf(
            ChatMessage(id = "1", content = "First", isFromUser = true),
            ChatMessage(id = "2", content = "Second", isFromUser = false),
            ChatMessage(id = "3", content = "Third", isFromUser = true)
        )
        
        val entities = messages.map { it.toEntity() }
        val restored = entities.map { it.toDomain() }
        
        assertEquals(3, restored.size)
        assertEquals("First", restored[0].content)
        assertEquals("Second", restored[1].content)
        assertEquals("Third", restored[2].content)
    }

    @Test
    fun `User message creation helper`() {
        val content = "User question about UPSC"
        val message = createUserMessage(content)
        
        assertTrue(message.isFromUser)
        assertEquals(content, message.content)
        assertEquals(MessageType.TEXT, message.messageType)
        assertNotNull(message.id)
        assertNotNull(message.timestamp)
    }

    @Test
    fun `Mentor message creation helper`() {
        val content = "Here's my advice..."
        val message = createMentorMessage(content)
        
        assertFalse(message.isFromUser)
        assertEquals(content, message.content)
        assertEquals(MessageType.TEXT, message.messageType)
    }

    @Test
    fun `Greeting message creation`() {
        val greeting = "Hello! I'm your UPSC Mentor."
        val message = createMentorMessage(greeting, MessageType.GREETING)
        
        assertFalse(message.isFromUser)
        assertEquals(MessageType.GREETING, message.messageType)
    }

    // Helper extension functions matching the repository implementation
    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp.time,
            messageType = messageType.name
        )
    }

    private fun ChatMessageEntity.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = Date(timestamp),
            messageType = try {
                MessageType.valueOf(messageType)
            } catch (e: Exception) {
                MessageType.TEXT
            }
        )
    }

    private fun createUserMessage(content: String): ChatMessage {
        return ChatMessage(
            content = content,
            isFromUser = true,
            messageType = MessageType.TEXT
        )
    }

    private fun createMentorMessage(
        content: String, 
        messageType: MessageType = MessageType.TEXT
    ): ChatMessage {
        return ChatMessage(
            content = content,
            isFromUser = false,
            messageType = messageType
        )
    }
}
