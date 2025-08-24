package com.example.mockmate.data

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.mockmate.data.database.AppDatabase
import com.example.mockmate.data.database.entities.QuestionEntity
import com.example.mockmate.data.database.entities.TestQuestionCrossRef
import com.example.mockmate.data.database.entities.UserStatsEntity
import com.example.mockmate.data.database.mappers.asDomainObject
import com.example.mockmate.data.database.mappers.asEntity
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.Question
import com.example.mockmate.model.QuestionDifficulty
import com.example.mockmate.model.QuestionStatus
import com.example.mockmate.model.QuestionType
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.model.UserAnswer
import com.example.mockmate.model.UserStats
import com.example.mockmate.model.SubjectPerformance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    
    // Removed _testAttempts and testAttempts StateFlow
    
    override val mockTests: Flow<List<MockTest>> = testDao.getAllTests()
        .map { testEntities ->
            testEntities.map { testEntity ->
                val questions = testDao.getQuestionsForTest(testEntity.id)
                testEntityToModel(testEntity, questions)
            }
        }
    
    override val userStats: Flow<UserStats> = userStatsDao.getUserStats()
        .map { userStatsEntity ->
            val userStats = userStatsEntity?.let { entityToUserStats(it) } ?: UserStats()
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
            testEntityToModel(testEntity, questions)
        }
    }

    override suspend fun getTestAttemptById(id: String): TestAttempt? {
        return withContext(Dispatchers.IO) {
            try {
                // Query directly from database
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
                            timeSpent = answerEntity.timeSpent, // Assuming timeSpent is available in UserAnswerEntity or can be defaulted
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
                    null // Skip this attempt if there's an error mapping it
                }
            }
            Log.d("TestRepositoryImpl", "Successfully mapped ${testAttempts.size} test attempts")
            testAttempts
        }
    }
    
    // Removed loadTestAttemptsFromDatabase()
    
    override suspend fun saveTest(test: MockTest) {
        withContext(Dispatchers.IO) {
            val testEntity = testModelToEntity(test)
            val questionEntities = test.questions.map { questionModelToEntity(it) }
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

                // Removed in-memory list update

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
            // Removed in-memory cache update
        }
    }

    override suspend fun updateTestAttemptCustomName(attemptId: String, customName: String) {
        withContext(Dispatchers.IO) {
            testAttemptDao.updateCustomName(attemptId, customName)
            // Removed in-memory cache update
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

                // Verify data after initialization
                val finalQuestionCount = questionDao.getQuestionCount()
                val testCount = testDao.getTestCount()
                val testAttemptCount = testAttemptDao.getTestAttemptCount()
                Log.d("TestRepositoryImpl", "Final verification - Questions: $finalQuestionCount, Tests: $testCount, TestAttempts: $testAttemptCount")

                // Generate sample test attempts if needed for analytics
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

            // Generate 2-3 attempts per test with different scores
            val sampleScores = listOf(65f, 75f, 85f, 90f, 95f)
            val calendar = java.util.Calendar.getInstance()

            tests.take(3).forEachIndexed { testIndex, testEntity ->
                val mockTest = testEntityToModel(testEntity, testDao.getQuestionsForTest(testEntity.id))

                sampleScores.forEachIndexed { scoreIndex, score ->
                    // Create attempt with some time variation
                    calendar.time = Date()
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, -(testIndex * 7 + scoreIndex * 2))

                    val attempt = TestAttempt(
                        id = UUID.randomUUID().toString(),
                        testId = mockTest.id,
                        startTime = calendar.time,
                        endTime = Date(calendar.timeInMillis + 1800000), // 30 minutes later
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
                                timeSpent = 45 + (questionIndex * 5), // Increasing time per question
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
                // Removed in-memory cache update for test attempts
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
        val questions = testDao.getQuestionsForTest(attempt.testId)
        Log.d("TestRepositoryImpl", "Loaded ${questions.size} questions")

        if (test == null) {
            Log.e("TestRepositoryImpl", "Test not found for attempt: ${attempt.id}")
            return
        }

        if (questions.isEmpty()) {
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

        val correctAnswers = questions.associate { it.id to it.correctOptionIndex }
        
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
    
    private fun questionModelToEntity(question: Question): QuestionEntity {
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
    
    private fun questionEntityToModel(entity: QuestionEntity): Question {
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
    
    private fun testModelToEntity(test: MockTest): com.example.mockmate.data.database.entities.TestEntity {
        return com.example.mockmate.data.database.entities.TestEntity(
            id = test.id,
            name = test.name,
            difficulty = test.difficulty.name,
            timeLimit = test.timeLimit,
            negativeMarking = test.negativeMarking,
            negativeMarkingValue = test.negativeMarkingValue
        )
    }
    
    private fun testEntityToModel(
        entity: com.example.mockmate.data.database.entities.TestEntity,
        questionEntities: List<QuestionEntity>
    ): MockTest {
        val questions = questionEntities.map { questionEntityToModel(it) }
        
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
    
    private fun entityToUserStats(entity: UserStatsEntity): UserStats {
        val subjectPerformanceMap = try {
            gson.fromJson<Map<String, SubjectPerformance>>(
                entity.subjectPerformance,
                object : TypeToken<Map<String, SubjectPerformance>>() {}.type
            )
        } catch (_: Exception) {
            mapOf<String, SubjectPerformance>()
        }
        
        return UserStats(
            questionsAnswered = entity.questionsAnswered,
            correctAnswers = entity.correctAnswers,
            currentStreak = entity.currentStreak, // Changed from streak
            longestStreak = entity.longestStreak, // Added longestStreak
            lastPracticeDate = entity.lastPracticeDate,
            subjectPerformance = subjectPerformanceMap
        )
    }
    
    private fun generateSampleTests(): List<MockTest> {
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
    
    private fun generateSampleQuestions(count: Int, subject: String, topic: String): List<Question> {
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
    
    companion object {
        @Volatile
        private var INSTANCE: TestRepositoryImpl? = null
    }
}
