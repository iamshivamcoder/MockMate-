package com.example.mockmate.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mockmate.data.database.entities.TestEntity
import com.example.mockmate.data.database.entities.TestQuestionCrossRef
import kotlinx.coroutines.flow.Flow

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
    suspend fun getQuestionsForTest(testId: String): List<com.example.mockmate.data.database.entities.QuestionEntity>
}