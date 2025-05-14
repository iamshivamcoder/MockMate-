package com.example.mockmate.data

import android.content.Context
import com.example.mockmate.data.database.AppDatabase
import com.example.mockmate.data.database.entities.QuestionEntity
import com.example.mockmate.data.database.entities.TestAttemptEntity
import com.example.mockmate.data.database.entities.TestEntity
import com.example.mockmate.data.database.entities.TestQuestionCrossRef
import com.example.mockmate.data.database.entities.UserAnswerEntity
import com.example.mockmate.data.database.entities.UserStatsEntity
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.Question
import com.example.mockmate.model.QuestionDifficulty
import com.example.mockmate.model.QuestionStatus
import com.example.mockmate.model.QuestionType
import com.example.mockmate.model.SubjectPerformance
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.model.UserAnswer
import com.example.mockmate.model.UserStats
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.random.Random

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
                testEntityToModel(testEntity, questions)
            }
        }
    
    override val userStats: Flow<UserStats> = userStatsDao.getUserStats()
        .map { userStatsEntity ->
            userStatsEntity?.let { entityToUserStats(it) } ?: UserStats()
        }
    
    override fun getTestsByDifficulty(difficulty: TestDifficulty): Flow<List<MockTest>> {
        return testDao.getTestsByDifficulty(difficulty.name)
            .map { testEntities ->
                testEntities.map { testEntity ->
                    val questions = testDao.getQuestionsForTest(testEntity.id)
                    testEntityToModel(testEntity, questions)
                }
            }
    }
    
    override suspend fun getTestById(id: String): MockTest? {
        return withContext(Dispatchers.IO) {
            val testEntity = testDao.getTestById(id) ?: return@withContext null
            val questions = testDao.getQuestionsForTest(id)
            testEntityToModel(testEntity, questions)
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
            val testAttemptEntity = TestAttemptEntity(
                id = attempt.id,
                testId = attempt.testId,
                startTime = attempt.startTime,
                endTime = attempt.endTime,
                isCompleted = attempt.isCompleted
            )
            
            val userAnswerEntities = attempt.userAnswers.map { (questionId, answer) ->
                UserAnswerEntity(
                    testAttemptId = attempt.id,
                    questionId = questionId,
                    selectedOptionIndex = answer.selectedOptionIndex,
                    timeSpent = answer.timeSpent,
                    status = answer.status.name
                )
            }
            
            testAttemptDao.insertTestAttemptWithAnswers(testAttemptEntity, userAnswerEntities)
            
            // Update stats if the attempt is completed
            if (attempt.isCompleted) {
                updateStats(attempt)
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
                
                // Initialize user stats
                userStatsDao.insertUserStats(UserStatsEntity())
            }
        }
    }
    
    private suspend fun updateStats(attempt: TestAttempt) {
        // Get the test and questions
        val test = testDao.getTestById(attempt.testId) ?: return
        val questions = testDao.getQuestionsForTest(attempt.testId)
        
        // Map of question IDs to their correct answers
        val correctAnswers = questions.associate { it.id to it.correctOptionIndex }
        
        // Count correct answers
        var correctCount = 0
        attempt.userAnswers.forEach { (questionId, answer) ->
            val correct = answer.selectedOptionIndex?.let { selectedIndex ->
                correctAnswers[questionId]?.let { correctIndex ->
                    selectedIndex == correctIndex
                }
            } ?: false
            
            if (correct) correctCount++
        }
        
        // Update user stats
        val answeredCount = attempt.userAnswers.count { it.value.selectedOptionIndex != null }
        userStatsDao.incrementQuestionsAnswered(answeredCount)
        userStatsDao.incrementCorrectAnswers(correctCount)
        
        // Update streak
        val today = Date()
        val statsEntity = userStatsDao.getUserStats().map { it ?: UserStatsEntity() }
        
        // Check if last practice was today to maintain streak
        statsEntity.collect { stats ->
            // Only increment streak if it's a new day
            val lastPracticeTimestamp = stats.lastPracticeDate?.time
            if (lastPracticeTimestamp == null || !isSameDay(lastPracticeTimestamp, today.time)) {
                userStatsDao.incrementStreak(today.time)
            }
        }
    }
    
    // Helper functions to convert between models and entities
    private fun questionModelToEntity(question: Question): QuestionEntity {
        return QuestionEntity(
            id = question.id,
            text = question.text,
            options = gson.toJson(question.options),
            correctOptionIndex = question.correctOptionIndex,
            explanation = question.explanation,
            difficulty = question.difficulty.name,
            type = question.type.name,
            subject = question.subject,
            topic = question.topic,
            timeRecommended = question.timeRecommended
        )
    }
    
    private fun questionEntityToModel(entity: QuestionEntity): Question {
        val options = gson.fromJson<List<String>>(
            entity.options,
            object : TypeToken<List<String>>() {}.type
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
            timeRecommended = entity.timeRecommended
        )
    }
    
    private fun testModelToEntity(test: MockTest): TestEntity {
        return TestEntity(
            id = test.id,
            name = test.name,
            difficulty = test.difficulty.name,
            timeLimit = test.timeLimit,
            negativeMarking = test.negativeMarking,
            negativeMarkingValue = test.negativeMarkingValue
        )
    }
    
    private fun testEntityToModel(
        entity: TestEntity, 
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
        // Parse the JSON string to a map of subject performances
        val subjectPerformanceMap = try {
            gson.fromJson<Map<String, SubjectPerformance>>(
                entity.subjectPerformance,
                object : TypeToken<Map<String, SubjectPerformance>>() {}.type
            )
        } catch (e: Exception) {
            mapOf<String, SubjectPerformance>()
        }
        
        return UserStats(
            questionsAnswered = entity.questionsAnswered,
            correctAnswers = entity.correctAnswers,
            streak = entity.streak,
            lastPracticeDate = entity.lastPracticeDate,
            subjectPerformance = subjectPerformanceMap  // Use the parsed subject performance map
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
    
    // Helper to generate sample questions based on UPSC subjects
    private fun generateSampleQuestions(count: Int, subject: String, topic: String): List<Question> {
        val questions = mutableListOf<Question>()
        
        // UPSC-specific sample questions based on subject
        val questionPairs = when (subject) {
            "Indian Polity" -> listOf(
                "Which article of the Indian Constitution deals with the Right to Equality?" to 
                    listOf("Article 14", "Article 19", "Article 21", "Article 32"),
                "Who is the constitutional head of the Indian state?" to 
                    listOf("President", "Prime Minister", "Chief Justice", "Speaker of Lok Sabha"),
                "How many fundamental rights are originally enshrined in the Indian Constitution?" to 
                    listOf("6", "7", "8", "9"),
                "Which amendment act is known as the Mini Constitution of India?" to 
                    listOf("42nd Amendment", "44th Amendment", "73rd Amendment", "86th Amendment"),
                "Which of the following is NOT a fundamental duty in the Indian Constitution?" to 
                    listOf("To pay income tax regularly", "To protect public property", "To protect the environment", "To safeguard public property")
            )
            "Economics" -> listOf(
                "Which of the following is NOT a function of the RBI?" to 
                    listOf("Fixing MSP for agricultural products", "Monetary policy regulation", "Foreign exchange management", "Issuing currency"),
                "NITI Aayog replaced which planning body in India?" to 
                    listOf("Planning Commission", "Finance Commission", "Economic Advisory Council", "National Development Council"),
                "Which tax was replaced by GST in India?" to 
                    listOf("VAT and Service Tax", "Income Tax", "Corporate Tax", "Wealth Tax"),
                "What is the objective of Jan Dhan Yojana?" to 
                    listOf("Financial inclusion", "Healthcare for all", "Housing for all", "Education for all"),
                "Which Five-Year Plan focused on sustainable growth?" to 
                    listOf("12th Five-Year Plan", "10th Five-Year Plan", "9th Five-Year Plan", "8th Five-Year Plan")
            )
            "History" -> listOf(
                "Who was the first Governor-General of independent India?" to 
                    listOf("C. Rajagopalachari", "Lord Mountbatten", "Dr. Rajendra Prasad", "Lord Wavell"),
                "The Revolt of 1857 started from which place?" to 
                    listOf("Meerut", "Delhi", "Kanpur", "Lucknow"),
                "Who founded the Indian National Congress in 1885?" to 
                    listOf("A.O. Hume", "W.C. Banerjee", "Dadabhai Naoroji", "Gopal Krishna Gokhale"),
                "The Quit India Movement was launched in which year?" to 
                    listOf("1942", "1930", "1940", "1947"),
                "Who gave the slogan 'Do or Die' during the freedom struggle?" to 
                    listOf("Mahatma Gandhi", "Subhas Chandra Bose", "Jawaharlal Nehru", "Bhagat Singh")
            )
            else -> listOf(
                "Sample question about $subject $topic?" to 
                    listOf("Option A", "Option B", "Option C", "Option D")
            )
        }
        
        // Generate questions using the subject-specific content
        for (i in 0 until count) {
            // Use modulo to cycle through available questions if count > available pairs
            val pairIndex = i % questionPairs.size
            val (questionText, options) = questionPairs[pairIndex]
            
            questions.add(
                Question(
                    id = UUID.randomUUID().toString(),
                    text = questionText,
                    options = options,
                    correctOptionIndex = 0, // First option is always correct for simplicity
                    explanation = "This is the explanation for this question about $subject.",
                    difficulty = when {
                        i % 3 == 0 -> QuestionDifficulty.EASY
                        i % 3 == 1 -> QuestionDifficulty.MEDIUM
                        else -> QuestionDifficulty.HARD
                    },
                    type = QuestionType.MULTIPLE_CHOICE,
                    subject = subject,
                    topic = topic,
                    timeRecommended = 60 // Default 60 seconds per question
                )
            )
        }
        
        return questions
    }
    
    companion object {
        @Volatile
        private var INSTANCE: TestRepositoryImpl? = null
        
        fun getInstance(context: Context): TestRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val instance = TestRepositoryImpl(context)
                INSTANCE = instance
                instance
            }
        }
        
        // Helper function to check if two timestamps represent the same day
        private fun isSameDay(timestamp1: Long?, timestamp2: Long): Boolean {
            if (timestamp1 == null) return false
            
            val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
            val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
            
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                   cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
        }
    }
}