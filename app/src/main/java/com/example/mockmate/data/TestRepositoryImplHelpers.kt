package com.example.mockmate.data

import com.example.mockmate.data.database.entities.QuestionEntity
import com.example.mockmate.data.database.entities.TestEntity
import com.example.mockmate.data.database.entities.UserStatsEntity
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.Question
import com.example.mockmate.model.QuestionDifficulty
import com.example.mockmate.model.QuestionType
import com.example.mockmate.model.SubjectPerformance
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.model.UserStats
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

internal fun questionModelToEntity(gson: Gson, question: Question): QuestionEntity {
    return QuestionEntity(
        id = question.id,
        text = question.text,
        options = gson.toJson(question.options),
        correctOptionIndex = question.correctOptionIndex ?: -1,
        explanation = question.explanation,
        difficulty = question.difficulty.name,
        type = question.type.name,
        subject = question.subject,
        topic = question.topic,
        timeRecommended = question.timeRecommended
    )
}

internal fun questionEntityToModel(gson: Gson, entity: QuestionEntity): Question {
    val options = gson.fromJson<List<String>?>(
        entity.options,
        object : TypeToken<List<String>?>() {}.type
    )

    return Question(
        id = entity.id,
        text = entity.text,
        options = options,
        correctOptionIndex = entity.correctOptionIndex,
        explanation = entity.explanation,
        difficulty = QuestionDifficulty.valueOf(entity.difficulty),
        type = QuestionType.valueOf(entity.type),
        subject = entity.subject,
        topic = entity.topic,
        timeRecommended = entity.timeRecommended,
        leftColumn = null,
        rightColumn = null,
        answers = null
    )
}

internal fun testModelToEntity(test: MockTest): TestEntity {
    return TestEntity(
        id = test.id,
        name = test.name,
        difficulty = test.difficulty.name,
        timeLimit = test.timeLimit,
        negativeMarking = test.negativeMarking,
        negativeMarkingValue = test.negativeMarkingValue
    )
}

internal fun testEntityToModel(
    entity: TestEntity,
    questionEntities: List<QuestionEntity>,
    gson: Gson
): MockTest {
    val questions = questionEntities.map { questionEntityToModel(gson, it) }

    return MockTest(
        id = entity.id,
        name = entity.name,
        difficulty = TestDifficulty.valueOf(entity.difficulty),
        questions = questions,
        timeLimit = entity.timeLimit,
        negativeMarking = entity.negativeMarking,
        negativeMarkingValue = entity.negativeMarkingValue
    )
}

internal fun entityToUserStats(gson: Gson, entity: UserStatsEntity): UserStats {
    val subjectPerformanceMap = try {
        gson.fromJson(
            entity.subjectPerformance,
            object : TypeToken<Map<String, SubjectPerformance>>() {}.type
        )
    } catch (_: Exception) {
        mapOf<String, SubjectPerformance>()
    }

    return UserStats(
        questionsAnswered = entity.questionsAnswered,
        correctAnswers = entity.correctAnswers,
        currentStreak = entity.currentStreak,
        longestStreak = entity.longestStreak,
        lastPracticeDate = entity.lastPracticeDate,
        subjectPerformance = subjectPerformanceMap
    )
}

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
