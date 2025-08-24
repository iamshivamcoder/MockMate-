package com.example.mockmate.data

import com.example.mockmate.model.MockTest
import com.example.mockmate.model.Question
import com.example.mockmate.model.QuestionDifficulty
import com.example.mockmate.model.QuestionType
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.model.UserStats
import com.example.mockmate.model.SubjectPerformance
import java.util.Date
import java.util.UUID

// Data class for MatchTheColumnScreen
data class MatchItem(val id: String, val text: String, val matchId: String)

internal fun generateSampleMatchItemsA(): List<MatchItem> {
    return listOf(
        MatchItem("a1", "Apple", "b1"),
        MatchItem("a2", "Banana", "b2"),
        MatchItem("a3", "Cherry", "b3"),
        MatchItem("a4", "Date", "b4")
    )
}

internal fun generateSampleMatchItemsB(): List<MatchItem> {
    return listOf(
        MatchItem("b1", "A fruit that is red or green", "a1"),
        MatchItem("b2", "A long yellow fruit", "a2"),
        MatchItem("b3", "A small red fruit", "a3"),
        MatchItem("b4", "A sweet brown fruit", "a4"),
        MatchItem("b5", "A citrus fruit", "") // Extra item
    ).shuffled()
}

internal fun generateSampleQuestionPreview(): Question {
    return Question(
        id = UUID.randomUUID().toString(),
        text = "What is the capital of France?",
        options = listOf("Berlin", "Madrid", "Paris", "Rome"),
        correctOptionIndex = 2,
        explanation = "Paris is the capital of France.",
        difficulty = QuestionDifficulty.EASY,
        subject = "Geography",
        topic = "World Capitals",
        timeRecommended = 60
    )
}

// Originally from TestRepositoryImplHelpers.kt
internal fun generateSampleQuestions(count: Int, subject: String, topic: String): List<Question> {
    val questions = mutableListOf<Question>()
    val questionPairs = when (subject) {
        "Indian Polity" -> listOf(
            "Which article of the Indian Constitution deals with the Right to Equality?" to
                listOf("Article 14", "Article 19", "Article 21", "Article 32"),
            "Who is the constitutional head of the Indian state?" to
                listOf("President", "Prime Minister", "Chief Justice", "Speaker of Lok Sabha"),
        )
        "Economics" -> listOf(
            "Which of the following is NOT a function of the RBI?" to
                listOf("Fixing MSP for agricultural products", "Monetary policy regulation", "Foreign exchange management", "Issuing currency"),
            "NITI Aayog replaced which planning body in India?" to
                listOf("Planning Commission", "Finance Commission", "Economic Advisory Council", "National Development Council"),
        )
        "History" -> listOf(
            "Who was the first Governor-General of independent India?" to
                listOf("C. Rajagopalachari", "Lord Mountbatten", "Dr. Rajendra Prasad", "Lord Wavell"),
            "The Revolt of 1857 started from which place?" to
                listOf("Meerut", "Delhi", "Kanpur", "Lucknow"),
        )
        else -> listOf(
            "Sample question about $subject $topic?" to
                listOf("Option A", "Option B", "Option C", "Option D")
        )
    }

    for (i in 0 until count) {
        val pairIndex = i % questionPairs.size
        val (questionText, options) = questionPairs[pairIndex]

        questions.add(
            Question(
                id = UUID.randomUUID().toString(),
                text = questionText,
                options = options,
                correctOptionIndex = 0,
                explanation = "This is the explanation for this question about $subject.",
                difficulty = when (i % 3) {
                    0 -> QuestionDifficulty.EASY
                    1 -> QuestionDifficulty.MEDIUM
                    else -> QuestionDifficulty.HARD
                },
                type = QuestionType.MULTIPLE_CHOICE,
                subject = subject,
                topic = topic,
                timeRecommended = 60,
                leftColumn = null,
                rightColumn = null,
                answers = null
            )
        )
    }
    return questions
}

// Originally from TestRepositoryImplHelpers.kt
internal fun generateSampleTests(): List<MockTest> {
    return listOf(
        MockTest(
            id = UUID.randomUUID().toString(),
            name = "Basic Indian Polity",
            difficulty = TestDifficulty.EASY,
            questions = generateSampleQuestions(10, "Indian Polity", "Constitution"),
            timeLimit = 30
        ),
        MockTest(
            id = UUID.randomUUID().toString(),
            name = "Indian Economy & Current Affairs",
            difficulty = TestDifficulty.MEDIUM,
            questions = generateSampleQuestions(20, "Economics", "National Economy"),
            timeLimit = 60
        ),
        MockTest(
            id = UUID.randomUUID().toString(),
            name = "Modern Indian History & Geography",
            difficulty = TestDifficulty.HARD,
            questions = generateSampleQuestions(30, "History", "Modern India"),
            timeLimit = 90
        )
    )
}

// Added function for sample UserStats
internal fun generateSampleUserStats(): UserStats {
    return UserStats(
        questionsAnswered = 100, // Matched to AnalyticsScreenPreview
        correctAnswers = 75,   // Matched to AnalyticsScreenPreview
        currentStreak = 5,     // Matched to AnalyticsScreenPreview
        longestStreak = 5,     // Matched to AnalyticsScreenPreview
        lastPracticeDate = Date(), // Added for completeness
        subjectPerformance = mapOf( // Added for completeness based on AnalyticsScreenPreview
            "Math" to SubjectPerformance("Math", 50, 30, 60f),
            "Science" to SubjectPerformance("Science", 30, 25, 83.33f),
            "History" to SubjectPerformance("History", 20, 10, 50f)
        )
    )
}

internal fun generateSampleTestAttemptsPreview(): List<TestAttempt> {
    return listOf(
        TestAttempt(id = "1", testId = "t1", startTime = Date(System.currentTimeMillis() - 86400000L * 2), score = 70f, isCompleted = true),
        TestAttempt(id = "2", testId = "t2", startTime = Date(System.currentTimeMillis() - 86400000L * 1), score = 85f, isCompleted = true),
        TestAttempt(id = "3", testId = "t3", startTime = Date(), score = 90f, isCompleted = true)
    )
}

internal fun getSampleCardTitle(): String {
    return "Sample Title"
}

internal fun getSampleErrorMessage(): String {
    return "This is a sample error message."
}
