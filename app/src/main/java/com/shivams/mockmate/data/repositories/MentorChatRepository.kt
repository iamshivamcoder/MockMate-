package com.shivams.mockmate.data.repositories

import com.shivams.mockmate.data.database.ChatDao
import com.shivams.mockmate.data.database.ChatMessageEntity
import com.shivams.mockmate.data.database.ChatSessionEntity
import com.shivams.mockmate.model.ChatMessage
import com.shivams.mockmate.model.MessageType
import com.shivams.mockmate.model.MentorPersona
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * Repository for managing mentor chat history and messages
 */
class MentorChatRepository(
    private val chatDao: ChatDao
) {
    /**
     * Get all chat messages as a Flow
     */
    fun getAllMessages(): Flow<List<ChatMessage>> {
        return chatDao.getAllMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get recent messages for context
     */
    suspend fun getRecentMessagesForContext(limit: Int = 10): List<ChatMessage> {
        return chatDao.getRecentMessages(limit).map { it.toDomain() }.reversed()
    }

    /**
     * Add a user message
     */
    suspend fun addUserMessage(content: String): ChatMessage {
        val message = ChatMessage(
            content = content,
            isFromUser = true,
            messageType = MessageType.TEXT
        )
        chatDao.insertMessage(message.toEntity())
        updateSessionMetadata()
        return message
    }

    /**
     * Add a mentor (AI) response
     */
    suspend fun addMentorMessage(content: String, messageType: MessageType = MessageType.TEXT): ChatMessage {
        val message = ChatMessage(
            content = content,
            isFromUser = false,
            messageType = messageType
        )
        chatDao.insertMessage(message.toEntity())
        updateSessionMetadata()
        return message
    }

    /**
     * Add the initial greeting message if chat is empty
     */
    suspend fun initializeChatIfEmpty() {
        val count = chatDao.getMessageCount()
        if (count == 0) {
            val greeting = ChatMessage(
                content = MentorPersona.DEFAULT.greeting,
                isFromUser = false,
                messageType = MessageType.GREETING
            )
            chatDao.insertMessage(greeting.toEntity())
            updateSessionMetadata()
        }
    }

    /**
     * Clear all chat history
     */
    suspend fun clearHistory() {
        chatDao.clearHistory()
        // Re-add greeting after clearing
        initializeChatIfEmpty()
    }

    /**
     * Get message count
     */
    suspend fun getMessageCount(): Int {
        return chatDao.getMessageCount()
    }

    private suspend fun updateSessionMetadata() {
        val count = chatDao.getMessageCount()
        val session = ChatSessionEntity(
            id = "default_session",
            lastUpdated = System.currentTimeMillis(),
            messageCount = count
        )
        chatDao.upsertSession(session)
    }

    // Extension functions for entity mapping
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

    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp.time,
            messageType = messageType.name
        )
    }
}
