package com.shivams.mockmate.data

import com.shivams.mockmate.data.database.entities.QuestionEntity
import com.shivams.mockmate.data.database.entities.TestEntity
import com.shivams.mockmate.data.database.entities.UserStatsEntity
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.Question
import com.shivams.mockmate.model.QuestionDifficulty
import com.shivams.mockmate.model.QuestionType
import com.shivams.mockmate.model.SubjectPerformance
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.model.UserStats
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
// Removed UUID import as it's no longer used here

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

// generateSampleQuestions was moved to SampleData.kt

// generateSampleTests was moved to SampleData.kt
