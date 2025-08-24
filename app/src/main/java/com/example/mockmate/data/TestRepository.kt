package com.example.mockmate.data

import com.example.mockmate.model.MockTest
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.model.UserStats
import kotlinx.coroutines.flow.Flow

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
