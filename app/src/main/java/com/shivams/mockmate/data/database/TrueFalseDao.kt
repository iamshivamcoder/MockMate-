package com.shivams.mockmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for True-False Aptitude Module operations.
 */
@Dao
interface TrueFalseDao {
    
    // Session operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrueFalseSessionEntity)
    
    @Update
    suspend fun updateSession(session: TrueFalseSessionEntity)
    
    @Query("SELECT * FROM true_false_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TrueFalseSessionEntity>>
    
    @Query("SELECT * FROM true_false_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): TrueFalseSessionEntity?
    
    @Query("SELECT * FROM true_false_sessions WHERE isCompleted = 1 ORDER BY startTime DESC LIMIT :limit")
    fun getRecentCompletedSessions(limit: Int = 10): Flow<List<TrueFalseSessionEntity>>
    
    @Query("DELETE FROM true_false_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)
    
    // Statement operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatements(statements: List<TrueFalseStatementEntity>)
    
    @Query("SELECT * FROM true_false_statements WHERE sessionId = :sessionId")
    suspend fun getStatementsForSession(sessionId: String): List<TrueFalseStatementEntity>
    
    @Query("DELETE FROM true_false_statements WHERE sessionId = :sessionId")
    suspend fun deleteStatementsForSession(sessionId: String)
    
    // Answer operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: TrueFalseAnswerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<TrueFalseAnswerEntity>)
    
    @Query("SELECT * FROM true_false_answers WHERE sessionId = :sessionId")
    suspend fun getAnswersForSession(sessionId: String): List<TrueFalseAnswerEntity>
    
    @Query("SELECT * FROM true_false_answers WHERE sessionId = :sessionId AND statementId = :statementId")
    suspend fun getAnswer(sessionId: String, statementId: String): TrueFalseAnswerEntity?
    
    // Statistics queries
    @Query("SELECT COUNT(*) FROM true_false_sessions WHERE isCompleted = 1")
    fun getTotalCompletedSessions(): Flow<Int>
    
    @Query("SELECT SUM(correctCount) FROM true_false_sessions WHERE isCompleted = 1")
    fun getTotalCorrectAnswers(): Flow<Int?>
    
    @Query("SELECT SUM(totalStatements) FROM true_false_sessions WHERE isCompleted = 1")
    fun getTotalStatementsAttempted(): Flow<Int?>
    
    @Query("SELECT AVG(score) FROM true_false_sessions WHERE isCompleted = 1")
    fun getAverageScore(): Flow<Float?>
    
    // Transaction for saving complete session with statements
    @Transaction
    suspend fun saveSessionWithStatements(
        session: TrueFalseSessionEntity,
        statements: List<TrueFalseStatementEntity>
    ) {
        insertSession(session)
        insertStatements(statements)
    }
}
