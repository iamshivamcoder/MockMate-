package com.example.mockmate.model

import java.util.Date
import java.util.UUID

// Enums
enum class QuestionDifficulty { EASY, MEDIUM, HARD }
enum class QuestionType { MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK, MATCH_THE_COLUMN }
enum class TestDifficulty { EASY, MEDIUM, HARD }
enum class PracticeMode { DAILY_CHALLENGE, FOCUSED_PRACTICE, CUSTOM_PRACTICE, MOCK_TEST, PARAGRAPH_ANALYSIS }
enum class QuestionStatus { UNATTEMPTED, ANSWERED, MARKED_FOR_REVIEW, BOOKMARKED }

// Question model
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val options: List<String>? = null,
    val correctOptionIndex: Int? = null,
    val explanation: String,
    val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val subject: String,
    val topic: String,
    val timeRecommended: Int = 60, // in seconds
    val leftColumn: List<String>? = null,
    val rightColumn: List<String>? = null,
    val answers: List<String>? = null
)

// Test models
data class MockTest(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val difficulty: TestDifficulty,
    val questions: List<Question>,
    val timeLimit: Int, // in minutes
    val negativeMarking: Boolean = false,
    val negativeMarkingValue: Float = 0.25f,
    val creationDate: Date = Date() // Added creationDate
)

// User attempt data
data class UserAnswer(
    val questionId: String,
    val selectedOptionIndex: Int?,
    val timeSpent: Int, // in seconds
    val status: QuestionStatus = QuestionStatus.UNATTEMPTED
)

data class TestAttempt(
    val id: String = UUID.randomUUID().toString(),
    val testId: String,
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val userAnswers: Map<String, UserAnswer> = mapOf(),
    val isCompleted: Boolean = false,
    val score: Float = 0f,
    val customName: String? = null
)

// User stats
data class UserStats(
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val streak: Int = 0,
    val lastPracticeDate: Date? = null,
    val subjectPerformance: Map<String, SubjectPerformance> = mapOf()
)

data class SubjectPerformance(
    val subject: String,
    val questionsAttempted: Int = 0,
    val correctAnswers: Int = 0,
    val accuracy: Float = 0f,
    val topicPerformance: Map<String, TopicPerformance> = mapOf()
)

data class TopicPerformance(
    val topic: String,
    val questionsAttempted: Int = 0,
    val correctAnswers: Int = 0,
    val accuracy: Float = 0f
)

// Settings
data class AppSettings(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderTime: String = "08:00",
    val defaultTestDifficulty: TestDifficulty = TestDifficulty.MEDIUM,
    val showExplanations: Boolean = true,
    val currentAffairsUpdates: Boolean = false,
    val optionalSubject: String = "Not Selected"
)

data class AttemptWithTest(
    val attemptId: String,
    val testId: String,
    val testName: String,
    val date: Date,
    val attemptedQuestions: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val score: Int
)


