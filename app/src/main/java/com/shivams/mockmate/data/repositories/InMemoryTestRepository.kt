package com.shivams.mockmate.data.repositories

import android.util.Log
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

    private val _mockTests = MutableStateFlow<List<MockTest>>(emptyList())

    override val mockTests: Flow<List<MockTest>> = _mockTests.asStateFlow()

    private val _userStats = MutableStateFlow(UserStats())

    override val userStats: Flow<UserStats> = _userStats.asStateFlow()

    private val _testAttempts = MutableStateFlow<MutableList<TestAttempt>>(mutableListOf())
    private val testAttempts: StateFlow<List<TestAttempt>> = _testAttempts.asStateFlow()

    override fun getTestsByDifficulty(difficulty: TestDifficulty): Flow<List<MockTest>> {
        return mockTests.map { tests -> tests.filter { it.difficulty == difficulty } }
    }

    override suspend fun getTestById(id: String): MockTest? {
        return _mockTests.value.find { it.id == id }
    }

    override suspend fun deleteMockTestById(testId: String) {
        val currentTests = _mockTests.value.toMutableList()
        val removed = currentTests.removeIf { it.id == testId }
        if (removed) {
            _mockTests.value = currentTests
            val currentAttempts = _testAttempts.value.toMutableList()
            val attemptsRemoved = currentAttempts.removeIf { it.testId == testId }
            if (attemptsRemoved) {
                _testAttempts.value = currentAttempts
            }
        }
    }

    override suspend fun getTestAttemptById(id: String): TestAttempt? {
        return _testAttempts.value.find { it.id == id }
    }

    override fun getAllTestAttempts(): Flow<List<TestAttempt>> {
        return testAttempts
    }

    override suspend fun saveTest(test: MockTest) {
        val currentTests = _mockTests.value.toMutableList()
        currentTests.removeIf { it.id == test.id }
        currentTests.add(test)
        _mockTests.value = currentTests
    }

    override suspend fun saveTestAttempt(attempt: TestAttempt) {
        try {
            val currentAttempts = _testAttempts.value.toMutableList()
            currentAttempts.removeIf { it.id == attempt.id }
            currentAttempts.add(attempt)
            _testAttempts.value = currentAttempts

            val currentStats = _userStats.value
            val correctAnswers = attempt.userAnswers.count { (questionId, answer) ->
                val question = _mockTests.value
                    .flatMap { it.questions }
                    .find { it.id == questionId }
                question?.correctOptionIndex == answer.selectedOptionIndex
            }
            _userStats.value = currentStats.copy(
                questionsAnswered = currentStats.questionsAnswered + attempt.userAnswers.size,
                correctAnswers = currentStats.correctAnswers + correctAnswers
            )
        } catch (e: Exception) {
            Log.e("TestRepository", "Error saving test attempt: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteTestAttempt(attemptId: String) {
        val currentAttempts = _testAttempts.value.toMutableList()
        val attemptRemoved = currentAttempts.removeIf { it.id == attemptId }
        if (attemptRemoved) {
            _testAttempts.value = currentAttempts
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

    override suspend fun initializeIfEmpty() {
        // This function is now empty as per your request.
    }

}