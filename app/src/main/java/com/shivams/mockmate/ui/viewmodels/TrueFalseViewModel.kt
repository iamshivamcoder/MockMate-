package com.shivams.mockmate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.database.TrueFalseAnswerEntity
import com.shivams.mockmate.data.database.TrueFalseDao
import com.shivams.mockmate.data.database.TrueFalseSessionEntity
import com.shivams.mockmate.data.database.TrueFalseStatementEntity
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.model.TrueFalseSession
import com.shivams.mockmate.model.TrueFalseStatement
import com.shivams.mockmate.service.AiInsightsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * UI State for True-False Aptitude Module
 */
data class TrueFalseUiState(
    // Configuration state
    val topic: String = "",
    val subject: String = "History",
    val difficulty: TestDifficulty = TestDifficulty.MEDIUM,
    val numberOfStatements: Int = 10,
    val negativeMarking: Boolean = true,
    val negativeMarkingValue: Float = 0.33f,
    
    // Generation state
    val isGenerating: Boolean = false,
    val generationError: String? = null,
    val topicError: String? = null,
    
    // Session state
    val session: TrueFalseSession? = null,
    val statements: List<TrueFalseStatement> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<String, Boolean?> = emptyMap(),
    val timeSpentPerStatement: Map<String, Int> = emptyMap(),
    
    // Timer state
    val sessionTimeElapsed: Int = 0,
    val currentStatementTime: Int = 0,
    
    // Feedback state
    val lastAnswerResult: AnswerResult? = null,
    val showExplanation: Boolean = false,
    
    // Result state
    val isCompleted: Boolean = false,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skippedCount: Int = 0,
    val finalScore: Float = 0f,
    
    // Text Import State
    val sourceText: String = "",
    val sourceTextError: String? = null
) {
    val currentStatement: TrueFalseStatement?
        get() = statements.getOrNull(currentIndex)
    
    val progress: Float
        get() = if (statements.isEmpty()) 0f else (currentIndex + 1).toFloat() / statements.size
    
    val answeredCount: Int
        get() = userAnswers.count { it.value != null }
    
    val canGoNext: Boolean
        get() = currentIndex < statements.size - 1
    
    val canGoPrevious: Boolean
        get() = currentIndex > 0
    
    val isLastStatement: Boolean
        get() = currentIndex == statements.size - 1
}

data class AnswerResult(
    val statementId: String,
    val userAnswer: Boolean?,
    val isCorrect: Boolean,
    val explanation: String,
    val trapWords: List<String>,
    val upscTip: String
)

@HiltViewModel
class TrueFalseViewModel @Inject constructor(
    private val aiInsightsService: AiInsightsService,
    private val trueFalseDao: TrueFalseDao
) : ViewModel() {
    
    companion object {
        private const val TAG = "TrueFalseVM"
    }
    
    private val _uiState = MutableStateFlow(TrueFalseUiState())
    val uiState: StateFlow<TrueFalseUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var sessionId: String = ""
    
    fun updateTopic(topic: String) {
        _uiState.update { it.copy(topic = topic, topicError = null) }
    }
    
    fun updateSubject(subject: String) {
        _uiState.update { it.copy(subject = subject) }
    }
    
    fun updateDifficulty(difficulty: TestDifficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }
    
    fun updateNumberOfStatements(count: Int) {
        _uiState.update { it.copy(numberOfStatements = count.coerceIn(5, 25)) }
    }
    
    fun updateNegativeMarking(enabled: Boolean) {
        _uiState.update { it.copy(negativeMarking = enabled) }
    }
    
    fun updateNegativeMarkingValue(value: Float) {
        _uiState.update { it.copy(negativeMarkingValue = value) }
    }
    
    fun updateSourceText(text: String) {
        _uiState.update { it.copy(sourceText = text, sourceTextError = null) }
    }

    fun generateStatementsFromText(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        
        if (state.sourceText.isBlank()) {
            _uiState.update { it.copy(sourceTextError = "Please enter some text") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, generationError = null) }
            
            try {
                val result = aiInsightsService.generateTrueFalseStatementsFromText(
                    text = state.sourceText,
                    numberOfStatements = state.numberOfStatements
                )
                
                result.fold(
                    onSuccess = { statements ->
                        sessionId = UUID.randomUUID().toString()
                        
                        val session = TrueFalseSession(
                            id = sessionId,
                            statements = statements,
                            subject = "Text Import", // Distinct subject for text mode
                            topic = "Custom Text",
                            difficulty = TestDifficulty.MEDIUM, // Default for text mode
                            negativeMarking = state.negativeMarking,
                            negativeMarkingValue = state.negativeMarkingValue
                        )
                        
                        saveSessionToDb(session, statements)
                        
                        _uiState.update { 
                            it.copy(
                                isGenerating = false,
                                session = session,
                                statements = statements,
                                currentIndex = 0,
                                userAnswers = emptyMap(),
                                timeSpentPerStatement = emptyMap()
                            ) 
                        }
                        
                        startTimer()
                        Log.d(TAG, "Generated ${statements.size} statements from text for session $sessionId")
                        onSuccess(sessionId)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to generate statements from text", error)
                        _uiState.update { 
                            it.copy(
                                isGenerating = false,
                                generationError = error.message ?: "Failed to generate statements from text"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during text statement generation", e)
                _uiState.update { 
                    it.copy(
                        isGenerating = false,
                        generationError = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }
    
    fun generateStatements(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        
        if (state.topic.isBlank()) {
            _uiState.update { it.copy(topicError = "Please enter a topic") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, generationError = null) }
            
            try {
                val result = aiInsightsService.generateTrueFalseStatements(
                    topic = state.topic,
                    subject = state.subject,
                    difficulty = state.difficulty,
                    numberOfStatements = state.numberOfStatements
                )
                
                result.fold(
                    onSuccess = { statements ->
                        sessionId = UUID.randomUUID().toString()
                        
                        val session = TrueFalseSession(
                            id = sessionId,
                            statements = statements,
                            subject = state.subject,
                            topic = state.topic,
                            difficulty = state.difficulty,
                            negativeMarking = state.negativeMarking,
                            negativeMarkingValue = state.negativeMarkingValue
                        )
                        
                        saveSessionToDb(session, statements)
                        
                        _uiState.update { 
                            it.copy(
                                isGenerating = false,
                                session = session,
                                statements = statements,
                                currentIndex = 0,
                                userAnswers = emptyMap(),
                                timeSpentPerStatement = emptyMap()
                            ) 
                        }
                        
                        startTimer()
                        Log.d(TAG, "Generated ${statements.size} statements for session $sessionId")
                        onSuccess(sessionId)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to generate statements", error)
                        _uiState.update { 
                            it.copy(
                                isGenerating = false,
                                generationError = error.message ?: "Failed to generate statements"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during statement generation", e)
                _uiState.update { 
                    it.copy(
                        isGenerating = false,
                        generationError = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }
    
    fun answerTrue() { recordAnswer(userAnswer = true) }
    fun answerFalse() { recordAnswer(userAnswer = false) }
    fun skipStatement() { recordAnswer(userAnswer = null) }
    
    private fun recordAnswer(userAnswer: Boolean?) {
        val state = _uiState.value
        val currentStatement = state.currentStatement ?: return
        
        val isCorrect = userAnswer == currentStatement.isTrue
        
        val result = AnswerResult(
            statementId = currentStatement.id,
            userAnswer = userAnswer,
            isCorrect = if (userAnswer != null) isCorrect else false,
            explanation = currentStatement.explanation,
            trapWords = currentStatement.trapWords,
            upscTip = currentStatement.upscTip
        )
        
        val newAnswers = state.userAnswers.toMutableMap()
        newAnswers[currentStatement.id] = userAnswer
        
        val newTimeSpent = state.timeSpentPerStatement.toMutableMap()
        newTimeSpent[currentStatement.id] = state.currentStatementTime
        
        viewModelScope.launch {
            saveAnswerToDb(currentStatement.id, userAnswer, state.currentStatementTime)
        }
        
        _uiState.update { 
            it.copy(
                userAnswers = newAnswers,
                timeSpentPerStatement = newTimeSpent,
                lastAnswerResult = result,
                showExplanation = true,
                currentStatementTime = 0
            ) 
        }
    }
    
    fun dismissExplanation() {
        _uiState.update { it.copy(showExplanation = false, lastAnswerResult = null) }
    }
    
    fun nextStatement() {
        val state = _uiState.value
        if (state.canGoNext) {
            _uiState.update { 
                it.copy(
                    currentIndex = state.currentIndex + 1,
                    showExplanation = false,
                    lastAnswerResult = null,
                    currentStatementTime = 0
                ) 
            }
        }
    }
    
    fun previousStatement() {
        val state = _uiState.value
        if (state.canGoPrevious) {
            _uiState.update { 
                it.copy(
                    currentIndex = state.currentIndex - 1,
                    showExplanation = false,
                    lastAnswerResult = null
                ) 
            }
        }
    }
    
    fun finishSession() {
        stopTimer()
        
        val state = _uiState.value
        val statements = state.statements
        val answers = state.userAnswers
        
        var correctCount = 0
        var incorrectCount = 0
        var skippedCount = 0
        
        for (statement in statements) {
            val userAnswer = answers[statement.id]
            when {
                userAnswer == null -> skippedCount++
                userAnswer == statement.isTrue -> correctCount++
                else -> incorrectCount++
            }
        }
        
        val positiveMarks = correctCount * 2f
        val negativeMarks = if (state.negativeMarking) {
            incorrectCount * state.negativeMarkingValue * 2f
        } else { 0f }
        val finalScore = positiveMarks - negativeMarks
        
        viewModelScope.launch {
            updateSessionInDb(correctCount, incorrectCount, skippedCount, finalScore)
        }
        
        _uiState.update { 
            it.copy(
                isCompleted = true,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                skippedCount = skippedCount,
                finalScore = finalScore
            ) 
        }
        
        Log.d(TAG, "Session completed: correct=$correctCount, incorrect=$incorrectCount, skipped=$skippedCount, score=$finalScore")
    }
    
    fun resetSession() {
        stopTimer()
        _uiState.value = TrueFalseUiState()
    }
    
    fun dismissError() {
        _uiState.update { it.copy(generationError = null) }
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { 
                    it.copy(
                        sessionTimeElapsed = it.sessionTimeElapsed + 1,
                        currentStatementTime = it.currentStatementTime + 1
                    ) 
                }
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
    
    private suspend fun saveSessionToDb(session: TrueFalseSession, statements: List<TrueFalseStatement>) {
        val sessionEntity = TrueFalseSessionEntity(
            id = session.id,
            startTime = session.startTime,
            endTime = null,
            isCompleted = false,
            subject = session.subject,
            topic = session.topic,
            difficulty = session.difficulty.name,
            negativeMarking = session.negativeMarking,
            negativeMarkingValue = session.negativeMarkingValue,
            totalStatements = statements.size
        )
        
        val statementEntities = statements.map { statement ->
            TrueFalseStatementEntity(
                id = statement.id,
                sessionId = session.id,
                statement = statement.statement,
                isTrue = statement.isTrue,
                explanation = statement.explanation,
                trapWords = JSONArray(statement.trapWords).toString(),
                upscTip = statement.upscTip,
                difficulty = statement.difficulty.name,
                subject = statement.subject,
                topic = statement.topic
            )
        }
        
        trueFalseDao.saveSessionWithStatements(sessionEntity, statementEntities)
    }
    
    private suspend fun saveAnswerToDb(statementId: String, userAnswer: Boolean?, timeSpent: Int) {
        val answerEntity = TrueFalseAnswerEntity(
            sessionId = sessionId,
            statementId = statementId,
            userAnswer = userAnswer,
            timeSpent = timeSpent
        )
        trueFalseDao.insertAnswer(answerEntity)
    }
    
    private suspend fun updateSessionInDb(
        correctCount: Int,
        incorrectCount: Int,
        skippedCount: Int,
        finalScore: Float
    ) {
        val existingSession = trueFalseDao.getSessionById(sessionId) ?: return
        val updatedSession = existingSession.copy(
            endTime = Date(),
            isCompleted = true,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            skippedCount = skippedCount,
            score = finalScore
        )
        trueFalseDao.updateSession(updatedSession)
    }
    
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            val sessionEntity = trueFalseDao.getSessionById(sessionId)
            if (sessionEntity != null) {
                // Determine difficulty enum safely
                val difficulty = try {
                    TestDifficulty.valueOf(sessionEntity.difficulty)
                } catch (e: Exception) {
                    TestDifficulty.MEDIUM
                }

                // Fetch statements first (needed for session constructor)
                val statementEntities = trueFalseDao.getStatementsForSession(sessionId)
                val statements = statementEntities.map { entity ->
                    TrueFalseStatement(
                        id = entity.id,
                        statement = entity.statement,
                        isTrue = entity.isTrue,
                        explanation = entity.explanation,
                        trapWords = try {
                            val jsonArray = JSONArray(entity.trapWords)
                            List(jsonArray.length()) { i -> jsonArray.getString(i) }
                        } catch (e: Exception) {
                            emptyList()
                        },
                        upscTip = entity.upscTip,
                        difficulty = difficulty.toQuestionDifficulty(),
                        subject = entity.subject,
                        topic = entity.topic
                    )
                }

                // Create session object with statements
                val session = TrueFalseSession(
                    id = sessionEntity.id,
                    startTime = sessionEntity.startTime,
                    subject = sessionEntity.subject,
                    topic = sessionEntity.topic,
                    difficulty = difficulty,
                    negativeMarking = sessionEntity.negativeMarking,
                    negativeMarkingValue = sessionEntity.negativeMarkingValue,
                    statements = statements
                )
                
                // Fetch existing answers to restore progress if re-entering
                val answerEntities = trueFalseDao.getAnswersForSession(sessionId)
                val userAnswers = answerEntities.associate { it.statementId to it.userAnswer }
                val timeSpent = answerEntities.associate { it.statementId to it.timeSpent }

                // Find first unanswered index
                var firstUnanswered = 0
                for (i in statements.indices) {
                    if (!userAnswers.containsKey(statements[i].id)) {
                        firstUnanswered = i
                        break
                    }
                }
                
                // Restore ID for saving further progress
                this@TrueFalseViewModel.sessionId = sessionId
                
                _uiState.update { 
                    it.copy(
                        session = session,
                        statements = statements,
                        currentIndex = firstUnanswered,
                        userAnswers = userAnswers,
                        timeSpentPerStatement = timeSpent,
                        topic = session.topic,
                        subject = session.subject,
                        difficulty = session.difficulty,
                        isCompleted = sessionEntity.isCompleted
                    ) 
                }
                
                if (!sessionEntity.isCompleted) {
                    startTimer()
                }
            } else {
                Log.e(TAG, "Session $sessionId not found")
                _uiState.update { it.copy(generationError = "Session not found") }
            }
        }
    }

    private fun TestDifficulty.toQuestionDifficulty(): com.shivams.mockmate.model.QuestionDifficulty {
        return when (this) {
            TestDifficulty.EASY -> com.shivams.mockmate.model.QuestionDifficulty.EASY
            TestDifficulty.MEDIUM -> com.shivams.mockmate.model.QuestionDifficulty.MEDIUM
            TestDifficulty.HARD -> com.shivams.mockmate.model.QuestionDifficulty.HARD
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
