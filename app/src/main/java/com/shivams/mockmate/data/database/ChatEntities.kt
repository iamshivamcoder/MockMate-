package com.shivams.mockmate.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shivams.mockmate.model.MessageType

/**
 * Room entity for storing chat messages
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val messageType: String = MessageType.TEXT.name
)

/**
 * Room entity for storing chat session metadata
 */
@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String = "default_session",
    val lastUpdated: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)
