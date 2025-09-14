package com.shivams.mockmate.data.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.room.withTransaction
import com.google.gson.Gson
import com.shivams.mockmate.data.database.AppDatabase
import com.shivams.mockmate.data.database.TestQuestionCrossRef
import com.shivams.mockmate.data.database.UserAnswerEntity
import com.shivams.mockmate.data.database.UserStatsEntity
import com.shivams.mockmate.data.database.asDomainObject
import com.shivams.mockmate.data.database.asEntity
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.QuestionStatus
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.model.UserAnswer
import com.shivams.mockmate.model.UserStats
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

    private val database by lazy { AppDatabase.Companion.getInstance(context) }
    private val questionDao by lazy { database.questionDao() }
    private val testDao by lazy { database.testDao() }
    private val testAttemptDao by lazy { database.testAttemptDao() }
    private val userStatsDao by lazy { database.userStatsDao() }
    private val gson = Gson()

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("mockmate_prefs", Context.MODE_PRIVATE)
    }

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
            database.withTransaction {
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
                        UserAnswerEntity(
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
        // This function is now empty as per your request.
    }

    override suspend fun deleteMockTestById(testId: String) {
        withContext(Dispatchers.IO) {
            try {
                database.withTransaction {
                    testAttemptDao.deleteAttemptsByTestId(testId)
                    testDao.deleteCrossRefsByTestId(testId)
                    testDao.deleteTestEntityById(testId)
                }
                Log.d(
                    "TestRepositoryImpl",
                    "Successfully deleted test $testId and its associated data."
                )
            } catch (e: Exception) {
                Log.e("TestRepositoryImpl", "Error deleting test $testId: ${e.message}", e)
                throw IllegalStateException(
                    "Failed to delete test $testId: ${e.localizedMessage}",
                    e
                )
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

                val isConsecutiveDay = calNextDayAfterLastPractice.get(Calendar.YEAR) == calToday.get(
                    Calendar.YEAR) &&
                                       calNextDayAfterLastPractice.get(Calendar.MONTH) == calToday.get(
                    Calendar.MONTH) &&
                                       calNextDayAfterLastPractice.get(Calendar.DAY_OF_MONTH) == calToday.get(
                    Calendar.DAY_OF_MONTH)

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

}