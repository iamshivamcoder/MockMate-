package com.example.mockmate.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mockmate.data.database.entities.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats LIMIT 1")
    fun getUserStats(): Flow<UserStatsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity)
    
    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)
    
    @Query("""
        UPDATE user_stats 
        SET questionsAnswered = questionsAnswered + :count 
        WHERE id = 1
    """)
    suspend fun incrementQuestionsAnswered(count: Int)
    
    @Query("""
        UPDATE user_stats 
        SET correctAnswers = correctAnswers + :count 
        WHERE id = 1
    """)
    suspend fun incrementCorrectAnswers(count: Int)
    
    @Query("""
        UPDATE user_stats 
        SET streak = streak + 1,
            lastPracticeDate = :date
        WHERE id = 1
    """)
    suspend fun incrementStreak(date: Long)
    
    @Query("""
        UPDATE user_stats 
        SET streak = 1,
            lastPracticeDate = :date
        WHERE id = 1
    """)
    suspend fun resetStreak(date: Long)
    
    @Query("""
        UPDATE user_stats 
        SET subjectPerformance = :performanceJson
        WHERE id = 1
    """)
    suspend fun updateSubjectPerformance(performanceJson: String)
}