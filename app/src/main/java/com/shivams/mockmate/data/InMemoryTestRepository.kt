package com.shivams.mockmate.data

import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory implementation of TestRepository for testing and development
 */
class InMemoryTestRepository : TestRepository {

    // Mock data for testing, now using centralized SampleData
    private val _mockTests = MutableStateFlow(generateSampleTests())

    override val mockTests: Flow<List<MockTest>> = _mockTests.asStateFlow()

    // Sample user stats, now using centralized SampleData
    private val _userStats = MutableStateFlow(generateSampleUserStats())

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
        // Nothing to do, tests already loaded using SampleData.generateSampleTests()
    }

    // Removed local generateSampleQuestions function as it's now centralized in SampleData.kt
}
