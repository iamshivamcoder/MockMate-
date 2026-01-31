package com.shivams.mockmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for analysis history operations.
 */
@Dao
interface AnalysisDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisEntity): Long
    
    @Query("SELECT * FROM analysis_history ORDER BY createdAt DESC")
    fun getAllAnalyses(): Flow<List<AnalysisEntity>>
    
    @Query("SELECT * FROM analysis_history ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestAnalysis(): AnalysisEntity?
    
    @Query("SELECT * FROM analysis_history WHERE id = :id")
    suspend fun getAnalysisById(id: Long): AnalysisEntity?
    
    @Query("DELETE FROM analysis_history WHERE id = :id")
    suspend fun deleteAnalysis(id: Long)
    
    @Query("DELETE FROM analysis_history")
    suspend fun deleteAllAnalyses()
    
    @Query("SELECT COUNT(*) FROM analysis_history")
    suspend fun getAnalysisCount(): Int
}
