package com.shivams.mockmate.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val text: String,
    val options: String, // Stored as JSON string
    val correctOptionIndex: Int,
    val explanation: String,
    val difficulty: String,
    val type: String,
    val subject: String,
    val topic: String,
    val timeRecommended: Int
)

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey val id: String,
    val name: String,
    val difficulty: String,
    val timeLimit: Int,
    val negativeMarking: Boolean,
    val negativeMarkingValue: Float
)

@Entity(
    tableName = "test_questions",
    primaryKeys = ["testId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("testId"),
        Index("questionId")
    ]
)
data class TestQuestionCrossRef(
    val testId: String,
    val questionId: String,
    val questionOrder: Int
)

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey val id: String,
    val testId: String,
    val startTime: Date,
    val endTime: Date?,
    val isCompleted: Boolean,
    val score: Float = 0f,
    val customName: String? = null
)

@Entity(
    tableName = "user_answers",
    primaryKeys = ["testAttemptId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = TestAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["testAttemptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("testAttemptId"),
        Index("questionId")
    ]
)
data class UserAnswerEntity(
    val testAttemptId: String,
    val questionId: String,
    val selectedOptionIndex: Int?,
    val timeSpent: Int,
    val status: String
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1, // Single row for user stats
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val currentStreak: Int = 0, // Changed from streak
    val longestStreak: Int = 0, // Added
    val lastPracticeDate: Date? = null,
    val subjectPerformance: String = "{}" // JSON string of subject performance
)
