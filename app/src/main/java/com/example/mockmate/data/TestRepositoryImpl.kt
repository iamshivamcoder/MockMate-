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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    
    private val _testAttempts = MutableStateFlow<MutableList<TestAttempt>>(mutableListOf())
    private val testAttempts: StateFlow<List<TestAttempt>> = _testAttempts.asStateFlow()
    
    override val mockTests: Flow<List<MockTest>> = testDao.getAllTests()
        .map { testEntities ->
            testEntities.map { testEntity ->
                val questions = testDao.getQuestionsForTest(testEntity.id)
                testEntityToModel(testEntity, questions)
            }
        }
    
    override val userStats: Flow<UserStats> = userStatsDao.getUserStats()
        .map { userStatsEntity ->
            userStatsEntity?.let { entityToUserStats(it) } ?: UserStats()
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
                // First check in-memory cache
                val cachedAttempt = _testAttempts.value.find { it.id == id }
                if (cachedAttempt != null) {
                    return@withContext cachedAttempt
                }

                // Query from database
                val attemptEntity = testAttemptDao.getTestAttemptById(id) ?: return@withContext null
                val userAnswerEntities = testAttemptDao.getUserAnswersForAttempt(id)

                // Convert entities to domain model without complex calculations
                val userAnswers = userAnswerEntities.associate { entity ->
                    entity.questionId to UserAnswer(
                        questionId = entity.questionId,
                        selectedOptionIndex = entity.selectedOptionIndex,
                        timeSpent = entity.timeSpent,
                        status = QuestionStatus.valueOf(entity.status)
                    )
                }

                val testAttempt = attemptEntity.asDomainObject().copy(userAnswers = userAnswers)

                // Add to in-memory cache
                val currentList = _testAttempts.value.toMutableList()
                currentList.removeIf { it.id == id } // Remove if exists
                currentList.add(testAttempt)
                _testAttempts.value = currentList

                testAttempt
            } catch (e: Exception) {
                Log.e("TestRepositoryImpl", "Error retrieving test attempt $id: ${e.message}", e)
                null
            }
        }
    }

    override fun getAllTestAttempts(): Flow<List<TestAttempt>> {
        return testAttempts
    }

    private suspend fun loadTestAttemptsFromDatabase() {
        try {
            val attemptEntities = testAttemptDao.getAllTestAttempts().firstOrNull() ?: emptyList()
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

                    entity.asDomainObject().copy(userAnswers = userAnswers)
                } catch (e: Exception) {
                    Log.e(
                        "TestRepositoryImpl",
                        "Error loading test attempt ${entity.id}: ${e.message}",
                        e
                    )
                    null
                }
            }

            _testAttempts.value = testAttempts.toMutableList()
            Log.d("TestRepositoryImpl", "Loaded ${testAttempts.size} test attempts from database")
        } catch (e: Exception) {
            Log.e(
                "TestRepositoryImpl",
                "Error loading test attempts from database: ${e.message}",
                e
            )
        }
    }
    
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
            
            // First save the questions
            questionDao.insertQuestions(questionEntities)
            
            // Then save the test with question references
            testDao.insertTestWithQuestions(testEntity, crossRefs)
        }
    }
    
    override suspend fun saveTestAttempt(attempt: TestAttempt) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("TestRepositoryImpl", "Starting to save test attempt: ${attempt.id}")

                // Validate the attempt data first
                if (attempt.testId.isBlank()) {
                    throw IllegalArgumentException("Test ID cannot be empty")
                }

                // Verify test exists
                val testExists = testDao.getTestById(attempt.testId) != null
                if (!testExists) {
                    throw IllegalArgumentException("Test with ID ${attempt.testId} does not exist")
                }

                // Create the test attempt entity
                val testAttemptEntity = attempt.asEntity()

                // Create user answer entities
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

                // Save everything in a single transaction with better error handling
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

                // Update in-memory list only after successful database save
                val currentList = _testAttempts.value.toMutableList()
                currentList.removeIf { it.id == attempt.id } // Remove if exists
                currentList.add(attempt)
                _testAttempts.value = currentList

                Log.d("TestRepositoryImpl", "Updated in-memory cache")

                // Update stats for completed tests
                if (attempt.isCompleted) {
                    try {
                        updateStats(attempt)
                        Log.d("TestRepositoryImpl", "Updated user stats")
                    } catch (e: Exception) {
                        Log.w("TestRepositoryImpl", "Failed to update stats, but test was saved", e)
                        // Don't fail the entire operation if stats update fails
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
            // Also remove from the in-memory cache
            val currentList = _testAttempts.value.toMutableList()
            currentList.removeIf { it.id == attemptId }
            _testAttempts.value = currentList
        }
    }

    override suspend fun updateTestAttemptCustomName(attemptId: String, customName: String) {
        withContext(Dispatchers.IO) {
            testAttemptDao.updateCustomName(attemptId, customName)
            // Also update the in-memory cache
            val currentList = _testAttempts.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == attemptId }
            if (index != -1) {
                val updatedAttempt = currentList[index].copy(customName = customName)
                currentList[index] = updatedAttempt
                _testAttempts.value = currentList
            }
        }
    }

    override suspend fun initializeIfEmpty() {
        withContext(Dispatchers.IO) {
            // Check if we already have questions and tests
            val questionCount = questionDao.getQuestionCount()
            if (questionCount == 0) {
                // Generate sample UPSC questions and tests
                val sampleTests = generateSampleTests()
                
                // Save each test and its questions
                sampleTests.forEach { test ->
                    saveTest(test)
                }

                // Initialize user stats using the correct DAO method
                userStatsDao.insertUserStats(UserStatsEntity())
            }

            // Always load existing test attempts from database
            loadTestAttemptsFromDatabase()
        }
    }

    override suspend fun deleteMockTestById(testId: String) {
        withContext(Dispatchers.IO) {
            try {
                database.withTransaction {
                    // Step 1: Delete all test attempts associated with this test
                    testAttemptDao.deleteAttemptsByTestId(testId)

                    // Step 2: Delete all TestQuestionCrossRef entries for this test
                    testDao.deleteCrossRefsByTestId(testId)

                    // Step 3: Delete the test entity itself
                    testDao.deleteTestEntityById(testId)
                }

                // Step 4: Update the in-memory cache of test attempts
                val currentAttempts = _testAttempts.value.toMutableList()
                val removed = currentAttempts.removeAll { it.testId == testId }
                if (removed) {
                    _testAttempts.value = currentAttempts
                }
                Log.d("TestRepositoryImpl", "Successfully deleted test $testId and its associated data.")
            } catch (e: Exception) {
                Log.e("TestRepositoryImpl", "Error deleting test $testId: ${e.message}", e)
                throw IllegalStateException("Failed to delete test $testId: ${e.localizedMessage}", e)
            }
        }
    }
    
    private suspend fun updateStats(attempt: TestAttempt) {
        // Get the test and questions
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

        // Get previous attempts for this test
        val previousAttempts = testAttemptDao.getTestAttemptsByTestId(attempt.testId)
            .firstOrNull()
            ?.filter { it.id != attempt.id && it.isCompleted }
            ?: emptyList()

        // Track which questions were previously answered
        val previouslyAnsweredQuestions = mutableSetOf<String>()
        previousAttempts.forEach { prevAttempt ->
            val prevAnswers = testAttemptDao.getUserAnswersForAttempt(prevAttempt.id)
            prevAnswers.forEach { answer ->
                if (answer.selectedOptionIndex != null) {
                    previouslyAnsweredQuestions.add(answer.questionId)
                }
            }
        }

        // Map of question IDs to their correct answers
        val correctAnswers = questions.associate { it.id to it.correctOptionIndex }
        
        // Count only new answered and correct questions in current attempt
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

        // Update user stats only for new unique questions
        if (answeredCount > 0) {
            userStatsDao.incrementQuestionsAnswered(answeredCount)
            userStatsDao.incrementCorrectAnswers(correctCount)
        }

        // Update streak
        val today = Date()
        val stats = userStatsDao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val lastPracticeTimestamp = stats.lastPracticeDate?.time

        if (lastPracticeTimestamp == null) {
            // First practice ever or stats reset
            userStatsDao.resetStreak(today.time)
            Log.d("TestRepositoryImpl", "Streak reset to 1 (first practice).")
        } else {
            val calLastPractice = Calendar.getInstance().apply { timeInMillis = lastPracticeTimestamp }
            val calToday = Calendar.getInstance().apply { timeInMillis = today.time }

            val isSameDay = calLastPractice.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                            calLastPractice.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) &&
                            calLastPractice.get(Calendar.DAY_OF_MONTH) == calToday.get(Calendar.DAY_OF_MONTH)

            if (!isSameDay) {
                // It's a new day
                val calNextDayAfterLastPractice = Calendar.getInstance().apply { timeInMillis = lastPracticeTimestamp }
                calNextDayAfterLastPractice.add(Calendar.DAY_OF_YEAR, 1)

                val isConsecutiveDay = calNextDayAfterLastPractice.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                                       calNextDayAfterLastPractice.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) &&
                                       calNextDayAfterLastPractice.get(Calendar.DAY_OF_MONTH) == calToday.get(Calendar.DAY_OF_MONTH)

                if (isConsecutiveDay) {
                    userStatsDao.incrementStreak(today.time)
                    Log.d("TestRepositoryImpl", "Streak incremented.")
                } else {
                    // Missed a day (or more)
                    userStatsDao.resetStreak(today.time)
                    Log.d("TestRepositoryImpl", "Streak reset to 1 (missed a day).")
                }
            } else {
                Log.d("TestRepositoryImpl", "Same day practice, streak maintained, no change in lastPracticeDate from here.")
                // If it's the same day, we don't necessarily update lastPracticeDate here.
                // resetStreak and incrementStreak already update it.
                // If no streak change occurs, lastPracticeDate remains from the earlier practice today.
            }
        }
    }
    
    // Helper functions to convert between models and entities
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
            streak = entity.streak,
            lastPracticeDate = entity.lastPracticeDate,
            subjectPerformance = subjectPerformanceMap
        )
    }
    
    // Sample data generation for initial setup
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
                // Add more relevant questions
            )
            "Economics" -> listOf(
                "Which of the following is NOT a function of the RBI?" to 
                    listOf("Fixing MSP for agricultural products", "Monetary policy regulation", "Foreign exchange management", "Issuing currency"),
                "NITI Aayog replaced which planning body in India?" to 
                    listOf("Planning Commission", "Finance Commission", "Economic Advisory Council", "National Development Council"),
                // Add more relevant questions
            )
            "History" -> listOf(
                "Who was the first Governor-General of independent India?" to 
                    listOf("C. Rajagopalachari", "Lord Mountbatten", "Dr. Rajendra Prasad", "Lord Wavell"),
                "The Revolt of 1857 started from which place?" to 
                    listOf("Meerut", "Delhi", "Kanpur", "Lucknow"),
                // Add more relevant questions
            )
            else -> listOf( // Default case
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

        // This helper function is no longer needed here as the logic is within updateStats
        // private fun inSameDay(timestamp1: Long?, timestamp2: Long): Boolean { ... }
    }
}
