package com.example.mockmate.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.mockmate.data.database.entities.QuestionEntity
import com.example.mockmate.data.database.entities.TestAttemptEntity
import com.example.mockmate.data.database.entities.TestEntity
import com.example.mockmate.data.database.entities.TestQuestionCrossRef
import com.example.mockmate.data.database.entities.UserAnswerEntity
import com.example.mockmate.data.database.entities.UserStatsEntity
import kotlinx.coroutines.flow.Flow

// Content from TestDao.kt
@Dao
interface TestDao {
    @Query("SELECT * FROM tests")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE id = :testId")
    suspend fun getTestById(testId: String): TestEntity?

    @Query("SELECT * FROM tests WHERE difficulty = :difficulty")
    fun getTestsByDifficulty(difficulty: String): Flow<List<TestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<TestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestQuestionCrossRefs(crossRefs: List<TestQuestionCrossRef>)

    @Transaction
    suspend fun insertTestWithQuestions(test: TestEntity, crossRefs: List<TestQuestionCrossRef>) {
        insertTest(test)
        insertTestQuestionCrossRefs(crossRefs)
    }

    @Query("DELETE FROM tests WHERE id = :testId")
    suspend fun deleteTest(testId: String)

    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN test_questions tq ON q.id = tq.questionId
        WHERE tq.testId = :testId
        ORDER BY tq.questionOrder
    """)
    suspend fun getQuestionsForTest(testId: String): List<QuestionEntity>
}

// Content from QuestionDao.kt
@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE subject = :subject")
    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subject = :subject AND topic = :topic")
    fun getQuestionsByTopic(subject: String, topic: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE difficulty = :difficulty")
    fun getQuestionsByDifficulty(difficulty: String): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestion(questionId: String)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int
}

// Content from UserStatsDao.kt
@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats LIMIT 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)

    @Query("""
        UPDATE user_stats
        SET questionsAnswered = questionsAnswered + :count
        WHERE id = 1
    """)
    suspend fun incrementQuestionsAnswered(count: Int)

    @Query("""
        UPDATE user_stats
        SET correctAnswers = correctAnswers + :count
        WHERE id = 1
    """)
    suspend fun incrementCorrectAnswers(count: Int)

    @Query("""
        UPDATE user_stats
        SET streak = streak + 1,
            lastPracticeDate = :date
        WHERE id = 1
    """)
    suspend fun incrementStreak(date: Long)

    @Query("""
        UPDATE user_stats
        SET streak = 1,
            lastPracticeDate = :date
        WHERE id = 1
    """)
    suspend fun resetStreak(date: Long)

    @Query("""
        UPDATE user_stats
        SET subjectPerformance = :performanceJson
        WHERE id = 1
    """)
    suspend fun updateSubjectPerformance(performanceJson: String)
}

// Content from TestAttemptDao.kt
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
