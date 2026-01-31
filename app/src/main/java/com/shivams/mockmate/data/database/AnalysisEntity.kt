package com.shivams.mockmate.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shivams.mockmate.model.analysis.QuestionAnalysis

/**
 * Room entity for storing PDF analysis results.
 */
@Entity(tableName = "analysis_history")
@TypeConverters(AnalysisConverters::class)
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val testSubject: String,
    val totalQuestions: Int,
    val analysisTimestamp: Long,
    val mentorFeedback: String,
    
    // Summary stats
    val solidCount: Int,
    val conceptCollapseCount: Int,
    val intuitionCount: Int,
    val flukeCount: Int,
    val doubtCount: Int,
    val correctCount: Int,
    val overallAccuracy: Float,
    
    // Serialized questions list
    val questionsJson: String,
    
    // Metadata
    val pdfFileName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Type converters for Room database.
 */
class AnalysisConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromQuestionsList(questions: List<QuestionAnalysis>): String {
        return gson.toJson(questions)
    }
    
    @TypeConverter
    fun toQuestionsList(json: String): List<QuestionAnalysis> {
        val type = object : TypeToken<List<QuestionAnalysis>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
