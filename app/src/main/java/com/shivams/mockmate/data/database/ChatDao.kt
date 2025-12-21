package com.shivams.mockmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat operations
 */
@Dao
interface ChatDao {
    
    /**
     * Get all chat messages ordered by timestamp
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>
    
    /**
     * Get recent messages (last N messages)
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ChatMessageEntity>
    
    /**
     * Insert a new message
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
    
    /**
     * Insert multiple messages
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)
    
    /**
     * Delete a specific message
     */
    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
    
    /**
     * Clear all chat history
     */
    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
    
    /**
     * Get message count
     */
    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
    
    /**
     * Get or create chat session
     */
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: String = "default_session"): ChatSessionEntity?
    
    /**
     * Insert or update session
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: ChatSessionEntity)
    
    /**
     * Update session metadata
     */
    @Update
    suspend fun updateSession(session: ChatSessionEntity)
}
