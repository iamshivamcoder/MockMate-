package com.example.mockmate.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.mockmate.data.database.entities.TestAttemptEntity
import com.example.mockmate.data.database.entities.UserAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestAttemptDao {
    @Query("SELECT * FROM test_attempts")
    fun getAllTestAttempts(): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE id = :attemptId")
    suspend fun getTestAttemptById(attemptId: String): TestAttemptEntity?

    @Query("UPDATE test_attempts SET customName = :customName WHERE id = :attemptId")
    suspend fun updateCustomName(attemptId: String, customName: String)

    @Query("SELECT * FROM test_attempts WHERE testId = :testId")
    fun getTestAttemptsByTestId(testId: String): Flow<List<TestAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestAttempt(attempt: TestAttemptEntity)

    @Update
    suspend fun updateTestAttempt(attempt: TestAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(userAnswer: UserAnswerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswers(userAnswers: List<UserAnswerEntity>)

    @Query("DELETE FROM user_answers WHERE testAttemptId = :attemptId")
    suspend fun deleteUserAnswersForAttempt(attemptId: String)

    @Query("DELETE FROM test_attempts WHERE id = :attemptId")
    suspend fun deleteTestAttemptById(attemptId: String)

    @Transaction
    suspend fun deleteAttemptAndAnswers(attemptId: String) {
        deleteUserAnswersForAttempt(attemptId)
        deleteTestAttemptById(attemptId)
    }

    @Transaction
    suspend fun insertTestAttemptWithAnswers(
        testAttempt: TestAttemptEntity,
        userAnswers: List<UserAnswerEntity>
    ) {
        // First clear any existing answers for this attempt
        deleteUserAnswersForAttempt(testAttempt.id)
        // Then insert the test attempt
        insertTestAttempt(testAttempt)
        // Finally insert all answers
        insertUserAnswers(userAnswers)
    }

    @Query("""
        SELECT * FROM user_answers
        WHERE testAttemptId = :attemptId
    """)
    suspend fun getUserAnswersForAttempt(attemptId: String): List<UserAnswerEntity>

    @Query("""
        SELECT COUNT(*) FROM user_answers
        WHERE testAttemptId = :attemptId AND selectedOptionIndex IS NOT NULL
    """)
    suspend fun getAnsweredQuestionsCount(attemptId: String): Int

    @Query("""
        SELECT AVG(
            CASE
                WHEN ua.selectedOptionIndex = q.correctOptionIndex THEN 1.0
                ELSE 0.0
            END
        )
        FROM user_answers ua
        JOIN questions q ON ua.questionId = q.id
        WHERE ua.testAttemptId = :attemptId AND ua.selectedOptionIndex IS NOT NULL
    """)
    suspend fun getAttemptAccuracy(attemptId: String): Float

    @Query("""
        SELECT subject,
        COUNT(*) as totalQuestions,
        SUM(CASE WHEN ua.selectedOptionIndex = q.correctOptionIndex THEN 1 ELSE 0 END) as correctAnswers
        FROM user_answers ua
        JOIN questions q ON ua.questionId = q.id
        WHERE ua.testAttemptId = :attemptId AND ua.selectedOptionIndex IS NOT NULL
        GROUP BY subject
    """)
    suspend fun getSubjectPerformance(attemptId: String): List<SubjectPerformance>

    @Query("""
        SELECT AVG(timeSpent)
        FROM user_answers
        WHERE testAttemptId = :attemptId AND selectedOptionIndex IS NOT NULL
    """)
    suspend fun getAverageTimePerQuestion(attemptId: String): Float

    data class SubjectPerformance(
        val subject: String,
        val totalQuestions: Int,
        val correctAnswers: Int
    )
}