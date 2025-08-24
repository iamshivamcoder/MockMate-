package com.example.mockmate.data

import com.example.mockmate.model.MockTest
import com.example.mockmate.model.Question
import com.example.mockmate.model.QuestionDifficulty
import com.example.mockmate.model.QuestionType
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repository that provides access to test and question data
 */
interface TestRepository {
    
    // Main data flows
    val mockTests: Flow<List<MockTest>>
    val userStats: Flow<UserStats>
    
    // Get tests by difficulty
    fun getTestsByDifficulty(difficulty: TestDifficulty): Flow<List<MockTest>>
    
    // Get a test by ID
    suspend fun getTestById(id: String): MockTest?
    
    // Get a test attempt by ID
    suspend fun getTestAttemptById(id: String): TestAttempt?

    // Get all test attempts
    fun getAllTestAttempts(): Flow<List<TestAttempt>>
    
    // Save a test
    suspend fun saveTest(test: MockTest)

    // Delete a mock test by ID
    suspend fun deleteMockTestById(testId: String)
    
    // Save a test attempt
    suspend fun saveTestAttempt(attempt: TestAttempt)

    // Delete a test attempt
    suspend fun deleteTestAttempt(attemptId: String)
    
    // Update a test attempt's custom name
    suspend fun updateTestAttemptCustomName(attemptId: String, customName: String)
    
    // Initialize repository with sample data if empty
    suspend fun initializeIfEmpty()
}

/**
 * In-memory implementation of TestRepository for testing and development
 */
class InMemoryTestRepository : TestRepository {
    
    // Mock data for testing
    private val _mockTests = MutableStateFlow<List<MockTest>>(
        listOf(
            MockTest(
                name = "Basic Indian Polity",
                difficulty = TestDifficulty.EASY,
                questions = generateSampleQuestions(10, "Indian Polity", "Constitution"),
                timeLimit = 30
            ),
            MockTest(
                name = "Indian Economy & Current Affairs",
                difficulty = TestDifficulty.MEDIUM,
                questions = generateSampleQuestions(20, "Economics", "National Economy"),
                timeLimit = 60
            ),
            MockTest(
                name = "Modern Indian History & Geography",
                difficulty = TestDifficulty.HARD,
                questions = generateSampleQuestions(30, "History", "Modern India"),
                timeLimit = 90
            )
        )
    )
    
    override val mockTests: Flow<List<MockTest>> = _mockTests.asStateFlow()
    
    // Sample user stats
    private val _userStats = MutableStateFlow(
        UserStats(
            questionsAnswered = 120,
            correctAnswers = 95,
            currentStreak = 7, // Changed from streak
            longestStreak = 7 // Assuming longestStreak should also be initialized
        )
    )
    
    override val userStats: Flow<UserStats> = _userStats.asStateFlow()
    
    // Test attempts
    private val _testAttempts = MutableStateFlow<MutableList<TestAttempt>>(mutableListOf())
    private val testAttempts: StateFlow<List<TestAttempt>> = _testAttempts.asStateFlow()
    
    // Get mock test by difficulty
    override fun getTestsByDifficulty(difficulty: TestDifficulty): Flow<List<MockTest>> {
        return mockTests.map { tests -> tests.filter { it.difficulty == difficulty } }
    }
    
    // Get a test by ID
    override suspend fun getTestById(id: String): MockTest? {
        return _mockTests.value.find { it.id == id }
    }

    // Delete a mock test by ID
    override suspend fun deleteMockTestById(testId: String) {
        val currentTests = _mockTests.value.toMutableList()
        val removed = currentTests.removeIf { it.id == testId }
        if (removed) {
            _mockTests.value = currentTests
            // Consider if associated test attempts should also be deleted.
            // For now, they will remain but might be orphaned if not handled elsewhere.
            // A more complete solution would also delete TestAttempts associated with this testId.
            val currentAttempts = _testAttempts.value.toMutableList()
            val attemptsRemoved = currentAttempts.removeIf { it.testId == testId }
            if (attemptsRemoved) {
                _testAttempts.value = currentAttempts
            }
        }
    }
    
    // Get a test attempt by ID
    override suspend fun getTestAttemptById(id: String): TestAttempt? {
        return _testAttempts.value.find { it.id == id }
    }

    // Get all test attempts
    override fun getAllTestAttempts(): Flow<List<TestAttempt>> {
        return testAttempts
    }
    
    // Save test
    override suspend fun saveTest(test: MockTest) {
        // First remove test with same ID if exists
        val currentTests = _mockTests.value.toMutableList()
        currentTests.removeIf { it.id == test.id }
        currentTests.add(test)
        _mockTests.value = currentTests
    }
    
    // Save test attempt
    override suspend fun saveTestAttempt(attempt: TestAttempt) {
        try {
            val currentAttempts = _testAttempts.value.toMutableList()
            // Remove any existing attempt with the same ID
            currentAttempts.removeIf { it.id == attempt.id }
            currentAttempts.add(attempt)
            _testAttempts.value = currentAttempts

            // Update user stats
            val currentStats = _userStats.value
            val correctAnswers = attempt.userAnswers.count { (questionId, answer) ->
                val question = _mockTests.value
                    .flatMap { it.questions }
                    .find { it.id == questionId }
                question?.correctOptionIndex == answer.selectedOptionIndex
            }
            // Streak update logic should be added here if needed
            _userStats.value = currentStats.copy(
                questionsAnswered = currentStats.questionsAnswered + attempt.userAnswers.size,
                correctAnswers = currentStats.correctAnswers + correctAnswers
                // currentStreak = ..., // Requires logic based on lastPracticeDate
                // longestStreak = ... // Requires logic based on lastPracticeDate and currentStreak
            )
        } catch (e: Exception) {
            android.util.Log.e("TestRepository", "Error saving test attempt: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteTestAttempt(attemptId: String) {
        val currentAttempts = _testAttempts.value.toMutableList()
        val attemptRemoved = currentAttempts.removeIf { it.id == attemptId }
        if (attemptRemoved) {
            _testAttempts.value = currentAttempts
            // Note: This simple implementation does not reverse the user stat changes.
            // A more robust implementation might need to recalculate stats or store attempt-specific stat changes.
        }
    }

    override suspend fun updateTestAttemptCustomName(attemptId: String, customName: String) {
        val currentAttempts = _testAttempts.value.toMutableList()
        val index = currentAttempts.indexOfFirst { it.id == attemptId }
        if (index != -1) {
            val updatedAttempt = currentAttempts[index].copy(customName = customName)
            currentAttempts[index] = updatedAttempt
            _testAttempts.value = currentAttempts
        }
    }

    // Initialize if empty - already initialized in constructor
    override suspend fun initializeIfEmpty() {
        // Nothing to do, tests already loaded in constructor
    }
    
    // Helper to generate sample questions
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
        repeat(count) { index ->
            // Use modulo to cycle through available questions if count > available pairs
            val pairIndex = index % questionPairs.size
            val (questionText, options) = questionPairs[pairIndex]
            
            questions.add(
                Question(
                    text = questionText,
                    options = options,
                    correctOptionIndex = 0, // First option is always correct for simplicity
                    explanation = "This is the explanation for this question about $subject.",
                    difficulty = when {
                        index % 3 == 0 -> QuestionDifficulty.EASY
                        index % 3 == 1 -> QuestionDifficulty.MEDIUM
                        else -> QuestionDifficulty.HARD
                    },
                    type = QuestionType.MULTIPLE_CHOICE,
                    subject = subject,
                    topic = topic
                )
            )
        }
        
        return questions
    }
}
