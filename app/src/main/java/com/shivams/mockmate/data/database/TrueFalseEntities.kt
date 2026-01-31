package com.shivams.mockmate.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity for storing True-False statements in the database.
 */
@Entity(tableName = "true_false_statements")
data class TrueFalseStatementEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val statement: String,
    val isTrue: Boolean,
    val explanation: String,
    val trapWords: String,  // JSON array of trap words
    val upscTip: String,
    val difficulty: String,
    val subject: String,
    val topic: String
)

/**
 * Entity for storing True-False practice sessions.
 */
@Entity(tableName = "true_false_sessions")
data class TrueFalseSessionEntity(
    @PrimaryKey val id: String,
    val startTime: Date,
    val endTime: Date?,
    val isCompleted: Boolean,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val negativeMarking: Boolean,
    val negativeMarkingValue: Float,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skippedCount: Int = 0,
    val totalStatements: Int = 0,
    val score: Float = 0f
)

/**
 * Entity for storing user answers to True-False statements.
 */
@Entity(
    tableName = "true_false_answers",
    primaryKeys = ["sessionId", "statementId"],
    foreignKeys = [
        ForeignKey(
            entity = TrueFalseSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("statementId")]
)
data class TrueFalseAnswerEntity(
    val sessionId: String,
    val statementId: String,
    val userAnswer: Boolean?,  // null = skipped
    val timeSpent: Int  // in seconds
)
