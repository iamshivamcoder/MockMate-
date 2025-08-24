package com.example.mockmate.data

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.mockmate.data.database.AppDatabase
import com.example.mockmate.data.database.entities.TestQuestionCrossRef
import com.example.mockmate.data.database.entities.UserStatsEntity
import com.example.mockmate.data.database.mappers.asDomainObject
import com.example.mockmate.data.database.mappers.asEntity
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.QuestionStatus
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.model.UserAnswer
import com.example.mockmate.model.UserStats
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Implementation of TestRepository that uses Room database for persistence
 */
class TestRepositoryImpl(
    private val context: Context
) : TestRepository {
    
    private val database by lazy { AppDatabase.getInstance(context) }
    private val questionDao by lazy { database.questionDao() }
    private val testDao by lazy { database.testDao() }
    private val testAttemptDao by lazy { database.testAttemptDao() }
    private val userStatsDao by lazy { database.userStatsDao() }
    private val gson = Gson()
    
    override val mockTests: Flow<List<MockTest>> = testDao.getAllTests()
        .map { testEntities ->
            testEntities.map { testEntity ->
                val questions = testDao.getQuestionsForTest(testEntity.id)
                testEntityToModel(testEntity, questions, gson)
            }
        }
    
    override val userStats: Flow<UserStats> = userStatsDao.getUserStats()
        .map { userStatsEntity ->
            val userStats = userStatsEntity?.let { entityToUserStats(gson, it) } ?: UserStats()
            Log.d("TestRepositoryImpl", "UserStats loaded: questionsAnswered=${userStats.questionsAnswered}, correctAnswers=${userStats.correctAnswers}, currentStreak=${userStats.currentStreak}, longestStreak=${userStats.longestStreak}, subjectPerformance=${userStats.subjectPerformance.size}")
            userStats
        }
    
    override fun getTestsByDifficulty(difficulty: TestDifficulty): Flow<List<MockTest>> {
        return mockTests.map { tests -> tests.filter { it.difficulty == difficulty } }
    }
    
    override suspend fun getTestById(id: String): MockTest? {
        return withContext(Dispatchers.IO) {
            val testEntity = testDao.getTestById(id) ?: return@withContext null
            val questions = testDao.getQuestionsForTest(id)
            testEntityToModel(testEntity, questions, gson)
        }
    }

    override suspend fun getTestAttemptById(id: String): TestAttempt? {
        return withContext(Dispatchers.IO) {
            try {
                val attemptEntity = testAttemptDao.getTestAttemptById(id) ?: return@withContext null
                val userAnswerEntities = testAttemptDao.getUserAnswersForAttempt(id)

                val userAnswers = userAnswerEntities.associate { entity ->
                    entity.questionId to UserAnswer(
                        questionId = entity.questionId,
                        selectedOptionIndex = entity.selectedOptionIndex,
                        timeSpent = entity.timeSpent,
                        status = QuestionStatus.valueOf(entity.status)
                    )
                }
                attemptEntity.asDomainObject().copy(userAnswers = userAnswers)
            } catch (e: Exception) {
                Log.e("TestRepositoryImpl", "Error retrieving test attempt $id: ${e.message}", e)
                null
            }
        }
    }

    override fun getAllTestAttempts(): Flow<List<TestAttempt>> {
        return testAttemptDao.getAllTestAttempts().map { attemptEntities ->
            Log.d("TestRepositoryImpl", "Loading ${attemptEntities.size} test attempts from database")
            val testAttempts = attemptEntities.mapNotNull { entity ->
                try {
                    val userAnswerEntities = testAttemptDao.getUserAnswersForAttempt(entity.id)
                    val userAnswers = userAnswerEntities.associate { answerEntity ->
                        answerEntity.questionId to UserAnswer(
                            questionId = answerEntity.questionId,
                            selectedOptionIndex = answerEntity.selectedOptionIndex,
                            timeSpent = answerEntity.timeSpent,
                            status = QuestionStatus.valueOf(answerEntity.status)
                        )
                    }
                    val testAttempt = entity.asDomainObject().copy(userAnswers = userAnswers)
                    Log.d("TestRepositoryImpl", "Mapped test attempt: ${testAttempt.id}, score=${testAttempt.score}, answers=${testAttempt.userAnswers.size}")
                    testAttempt
                } catch (e: Exception) {
                    Log.e(
                        "TestRepositoryImpl",
                        "Error mapping test attempt entity ${entity.id} for getAllTestAttempts: ${e.message}",
                        e
                    )
                    null
                }
            }
            Log.d("TestRepositoryImpl", "Successfully mapped ${testAttempts.size} test attempts")
            testAttempts
        }
    }
    
    override suspend fun saveTest(test: MockTest) {
        withContext(Dispatchers.IO) {
            val testEntity = testModelToEntity(test)
            val questionEntities = test.questions.map { questionModelToEntity(gson, it) }
            val crossRefs = test.questions.mapIndexed { index, question ->
                TestQuestionCrossRef(
                    testId = test.id,
                    questionId = question.id,
                    questionOrder = index
                )
            }
            
            questionDao.insertQuestions(questionEntities)
            testDao.insertTestWithQuestions(testEntity, crossRefs)
        }
    }
    
    override suspend fun saveTestAttempt(attempt: TestAttempt) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("TestRepositoryImpl", "Starting to save test attempt: ${attempt.id}")

                if (attempt.testId.isBlank()) {
                    throw IllegalArgumentException("Test ID cannot be empty")
                }

                val testExists = testDao.getTestById(attempt.testId) != null
                if (!testExists) {
                    throw IllegalArgumentException("Test with ID ${attempt.testId} does not exist")
                }

                val testAttemptEntity = attempt.asEntity()
                val userAnswerEntities = attempt.userAnswers.mapNotNull { (questionId, answer) ->
                    try {
                        com.example.mockmate.data.database.entities.UserAnswerEntity(
                            testAttemptId = attempt.id,
                            questionId = questionId,
                            selectedOptionIndex = answer.selectedOptionIndex,
                            timeSpent = answer.timeSpent,
                            status = answer.status.name
                        )
                    } catch (e: Exception) {
                        Log.w(
                            "TestRepositoryImpl",
                            "Skipping invalid answer for question $questionId: ${e.message}"
                        )
                        null
                    }
                }

                Log.d("TestRepositoryImpl", "Saving ${userAnswerEntities.size} user answers")

                database.withTransaction {
                    try {
                        testAttemptDao.insertTestAttemptWithAnswers(
                            testAttemptEntity,
                            userAnswerEntities
                        )
                        Log.d("TestRepositoryImpl", "Successfully saved test attempt to database")
                    } catch (e: Exception) {
                        Log.e("TestRepositoryImpl", "Database transaction failed", e)
                        throw IllegalStateException("Failed to save test data to database: ${e.localizedMessage}")
                    }
                }

                Log.d("TestRepositoryImpl", "In-memory cache update removed")

                if (attempt.isCompleted) {
                    try {
                        updateStats(attempt)
                        Log.d("TestRepositoryImpl", "Updated user stats")
                    } catch (e: Exception) {
                        Log.w("TestRepositoryImpl", "Failed to update stats, but test was saved", e)
                    }
                }

                Log.d("TestRepositoryImpl", "Test attempt saved successfully")

            } catch (e: IllegalArgumentException) {
                Log.e("TestRepositoryImpl", "Invalid test attempt data", e)
                throw e
            } catch (e: IllegalStateException) {
                Log.e("TestRepositoryImpl", "Failed to save test attempt", e)
                throw e
            } catch (e: Exception) {
                Log.e("TestRepositoryImpl", "Unexpected error saving test attempt", e)
                throw IllegalStateException("Could not save your test results. Please try again.")
            }
        }
    }
    
    override suspend fun deleteTestAttempt(attemptId: String) {
        withContext(Dispatchers.IO) {
            testAttemptDao.deleteAttemptAndAnswers(attemptId)
        }
    }

    override suspend fun updateTestAttemptCustomName(attemptId: String, customName: String) {
        withContext(Dispatchers.IO) {
            testAttemptDao.updateCustomName(attemptId, customName)
        }
    }

    override suspend fun initializeIfEmpty() {
        withContext(Dispatchers.IO) {
            try {
                val questionCount = questionDao.getQuestionCount()
                Log.d("TestRepositoryImpl", "Question count in database: $questionCount")

                if (questionCount == 0) {
                    Log.d("TestRepositoryImpl", "Database is empty, initializing with sample data")
                    val sampleTests = generateSampleTests()
                    Log.d("TestRepositoryImpl", "Generated ${sampleTests.size} sample tests")

                    sampleTests.forEach { test ->
                        saveTest(test)
                    }
                    userStatsDao.insertUserStats(UserStatsEntity())
                    Log.d("TestRepositoryImpl", "Sample data initialization completed")
                } else {
                    Log.d("TestRepositoryImpl", "Database already contains data, skipping initialization")
                }

                val finalQuestionCount = questionDao.getQuestionCount()
                val testCount = testDao.getTestCount()
                val testAttemptCount = testAttemptDao.getTestAttemptCount()
                Log.d("TestRepositoryImpl", "Final verification - Questions: $finalQuestionCount, Tests: $testCount, TestAttempts: $testAttemptCount")

                if (testAttemptCount == 0 && testCount > 0) {
                    Log.d("TestRepositoryImpl", "No test attempts found, generating sample attempts for analytics")
                    generateSampleTestAttempts()
                }

            } catch (e: Exception) {
                Log.e("TestRepositoryImpl", "Error during initialization: ${e.message}", e)
                throw e
            }
        }
    }

    private suspend fun generateSampleTestAttempts() {
        try {
            val tests = testDao.getAllTests().firstOrNull()
            if (tests.isNullOrEmpty()) {
                Log.d("TestRepositoryImpl", "No tests available to generate attempts")
                return
            }

            Log.d("TestRepositoryImpl", "Generating sample test attempts for ${tests.size} tests")

            val sampleScores = listOf(65f, 75f, 85f, 90f, 95f)
            val calendar = Calendar.getInstance()

            tests.take(3).forEachIndexed { testIndex, testEntity ->
                val mockTest = testEntityToModel(testEntity, testDao.getQuestionsForTest(testEntity.id), gson)

                sampleScores.forEachIndexed { scoreIndex, score ->
                    calendar.time = Date()
                    calendar.add(Calendar.DAY_OF_MONTH, -(testIndex * 7 + scoreIndex * 2))

                    val attempt = TestAttempt(
                        id = UUID.randomUUID().toString(),
                        testId = mockTest.id,
                        startTime = calendar.time,
                        endTime = Date(calendar.timeInMillis + 1800000),
                        userAnswers = mockTest.questions.mapIndexed { questionIndex, question ->
                            val isCorrect = when {
                                score >= 90 -> questionIndex < (mockTest.questions.size * 0.9).toInt()
                                score >= 80 -> questionIndex < (mockTest.questions.size * 0.8).toInt()
                                score >= 70 -> questionIndex < (mockTest.questions.size * 0.7).toInt()
                                else -> questionIndex < (mockTest.questions.size * 0.6).toInt()
                            }
                            question.id to UserAnswer(
                                questionId = question.id,
                                selectedOptionIndex = if (isCorrect) question.correctOptionIndex else (question.correctOptionIndex?.plus(1))?.rem(4),
                                timeSpent = 45 + (questionIndex * 5),
                                status = QuestionStatus.ANSWERED
                            )
                        }.toMap(),
                        isCompleted = true,
                        score = score
                    )

                    saveTestAttempt(attempt)
                    Log.d("TestRepositoryImpl", "Generated sample attempt: ${attempt.id} for test ${mockTest.name} with score $score")
                }
            }

            Log.d("TestRepositoryImpl", "Sample test attempts generation completed")

        } catch (e: Exception) {
            Log.e("TestRepositoryImpl", "Error generating sample test attempts: ${e.message}", e)
        }
    }

    override suspend fun deleteMockTestById(testId: String) {
        withContext(Dispatchers.IO) {
            try {
                database.withTransaction {
                    testAttemptDao.deleteAttemptsByTestId(testId)
                    testDao.deleteCrossRefsByTestId(testId)
                    testDao.deleteTestEntityById(testId)
                }
                Log.d("TestRepositoryImpl", "Successfully deleted test $testId and its associated data.")
            } catch (e: Exception) {
                Log.e("TestRepositoryImpl", "Error deleting test $testId: ${e.message}", e)
                throw IllegalStateException("Failed to delete test $testId: ${e.localizedMessage}", e)
            }
        }
    }
    
    private suspend fun updateStats(attempt: TestAttempt) {
        val test = testDao.getTestById(attempt.testId)
        Log.d("TestRepositoryImpl", "Loaded test: ${test?.name}")
        val questionsFromDb = testDao.getQuestionsForTest(attempt.testId)
        Log.d("TestRepositoryImpl", "Loaded ${questionsFromDb.size} questions")

        if (test == null) {
            Log.e("TestRepositoryImpl", "Test not found for attempt: ${attempt.id}")
            return
        }

        if (questionsFromDb.isEmpty()) {
            Log.e("TestRepositoryImpl", "Questions not found for test: ${attempt.testId}")
            return
        }

        val previousAttempts = testAttemptDao.getTestAttemptsByTestId(attempt.testId)
            .firstOrNull()
            ?.filter { it.id != attempt.id && it.isCompleted }
            ?: emptyList()

        val previouslyAnsweredQuestions = mutableSetOf<String>()
        previousAttempts.forEach { prevAttempt ->
            val prevAnswers = testAttemptDao.getUserAnswersForAttempt(prevAttempt.id)
            prevAnswers.forEach { answer ->
                if (answer.selectedOptionIndex != null) {
                    previouslyAnsweredQuestions.add(answer.questionId)
                }
            }
        }

        val correctAnswers = questionsFromDb.associate { it.id to it.correctOptionIndex }
        
        var correctCount = 0
        var answeredCount = 0
        attempt.userAnswers.forEach { (questionId, answer) ->
            if (answer.selectedOptionIndex != null && !previouslyAnsweredQuestions.contains(questionId)) {
                answeredCount++
                val correct = correctAnswers[questionId]?.let { correctIndex ->
                    answer.selectedOptionIndex == correctIndex
                } ?: false
                if (correct) correctCount++
            }
        }
        
        Log.d("TestRepositoryImpl", "New unique answered questions: $answeredCount")
        Log.d("TestRepositoryImpl", "New unique correct answers: $correctCount")

        if (answeredCount > 0) {
            userStatsDao.incrementQuestionsAnswered(answeredCount)
            userStatsDao.incrementCorrectAnswers(correctCount)
        }

        val today = Date()
        val stats = userStatsDao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val lastPracticeTimestamp = stats.lastPracticeDate?.time

        if (lastPracticeTimestamp == null) {
            userStatsDao.resetStreak(today.time)
            Log.d("TestRepositoryImpl", "Streak reset to 1 (first practice).")
        } else {
            val calLastPractice = Calendar.getInstance().apply { timeInMillis = lastPracticeTimestamp }
            val calToday = Calendar.getInstance().apply { timeInMillis = today.time }

            val isSameDay = calLastPractice.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                            calLastPractice.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) &&
                            calLastPractice.get(Calendar.DAY_OF_MONTH) == calToday.get(Calendar.DAY_OF_MONTH)

            if (!isSameDay) {
                val calNextDayAfterLastPractice = Calendar.getInstance().apply { timeInMillis = lastPracticeTimestamp }
                calNextDayAfterLastPractice.add(Calendar.DAY_OF_YEAR, 1)

                val isConsecutiveDay = calNextDayAfterLastPractice.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                                       calNextDayAfterLastPractice.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) &&
                                       calNextDayAfterLastPractice.get(Calendar.DAY_OF_MONTH) == calToday.get(Calendar.DAY_OF_MONTH)

                if (isConsecutiveDay) {
                    userStatsDao.incrementStreak(today.time)
                    Log.d("TestRepositoryImpl", "Streak incremented.")
                } else {
                    userStatsDao.resetStreak(today.time)
                    Log.d("TestRepositoryImpl", "Streak reset to 1 (missed a day).")
                }
            } else {
                Log.d("TestRepositoryImpl", "Same day practice, streak maintained, no change in lastPracticeDate from here.")
            }
        }
    }
    
    companion object
}
