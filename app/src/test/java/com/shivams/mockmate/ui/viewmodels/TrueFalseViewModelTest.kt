package com.shivams.mockmate.ui.viewmodels

import com.shivams.mockmate.model.QuestionDifficulty
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.model.TrueFalseStatement
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TrueFalseViewModel state management
 * Tests the UI state logic without requiring Android dependencies
 */
class TrueFalseViewModelTest {

    @Test
    fun `initial TrueFalseUiState has default values`() {
        val state = TrueFalseUiState()
        
        assertEquals("", state.topic)
        assertEquals("History", state.subject)
        assertEquals(TestDifficulty.MEDIUM, state.difficulty)
        assertEquals(10, state.numberOfStatements)
        assertTrue(state.negativeMarking)
        assertEquals(0.33f, state.negativeMarkingValue, 0.01f)
        assertFalse(state.isGenerating)
        assertNull(state.generationError)
        assertNull(state.session)
        assertTrue(state.statements.isEmpty())
        assertEquals(0, state.currentIndex)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `TrueFalseUiState topic can be updated`() {
        val initialState = TrueFalseUiState()
        val updatedState = initialState.copy(topic = "Indian Polity")
        
        assertEquals("", initialState.topic)
        assertEquals("Indian Polity", updatedState.topic)
    }

    @Test
    fun `TrueFalseUiState subject can be updated`() {
        val initialState = TrueFalseUiState()
        val updatedState = initialState.copy(subject = "Geography")
        
        assertEquals("History", initialState.subject)
        assertEquals("Geography", updatedState.subject)
    }

    @Test
    fun `TrueFalseUiState difficulty can be changed`() {
        val easy = TrueFalseUiState(difficulty = TestDifficulty.EASY)
        val hard = easy.copy(difficulty = TestDifficulty.HARD)
        
        assertEquals(TestDifficulty.EASY, easy.difficulty)
        assertEquals(TestDifficulty.HARD, hard.difficulty)
    }

    @Test
    fun `TrueFalseUiState numberOfStatements can be updated`() {
        val state = TrueFalseUiState()
        val updated = state.copy(numberOfStatements = 15)
        
        assertEquals(10, state.numberOfStatements)
        assertEquals(15, updated.numberOfStatements)
    }

    @Test
    fun `TrueFalseUiState negativeMarking can be toggled`() {
        val enabled = TrueFalseUiState(negativeMarking = true)
        val disabled = enabled.copy(negativeMarking = false)
        
        assertTrue(enabled.negativeMarking)
        assertFalse(disabled.negativeMarking)
    }

    @Test
    fun `TrueFalseUiState currentStatement returns correct statement`() {
        val statements = listOf(
            createStatement("1", "Statement 1", true),
            createStatement("2", "Statement 2", false),
            createStatement("3", "Statement 3", true)
        )
        
        val state = TrueFalseUiState(statements = statements, currentIndex = 1)
        
        assertNotNull(state.currentStatement)
        assertEquals("Statement 2", state.currentStatement?.statement)
        assertFalse(state.currentStatement?.isTrue ?: true)
    }

    @Test
    fun `TrueFalseUiState currentStatement returns null for empty list`() {
        val state = TrueFalseUiState(statements = emptyList())
        
        assertNull(state.currentStatement)
    }

    @Test
    fun `TrueFalseUiState progress calculation is correct`() {
        val statements = listOf(
            createStatement("1", "S1", true),
            createStatement("2", "S2", false),
            createStatement("3", "S3", true),
            createStatement("4", "S4", false)
        )
        
        val stateAt0 = TrueFalseUiState(statements = statements, currentIndex = 0)
        val stateAt1 = TrueFalseUiState(statements = statements, currentIndex = 1)
        val stateAt3 = TrueFalseUiState(statements = statements, currentIndex = 3)
        
        assertEquals(0.25f, stateAt0.progress, 0.01f)
        assertEquals(0.5f, stateAt1.progress, 0.01f)
        assertEquals(1.0f, stateAt3.progress, 0.01f)
    }

    @Test
    fun `TrueFalseUiState progress is zero for empty statements`() {
        val state = TrueFalseUiState(statements = emptyList())
        
        assertEquals(0f, state.progress, 0.01f)
    }

    @Test
    fun `TrueFalseUiState answeredCount is correct`() {
        val answers = mapOf(
            "1" to true,
            "2" to false,
            "3" to null  // Skipped
        )
        
        val state = TrueFalseUiState(userAnswers = answers)
        
        // Only non-null answers count (2 in this case)
        assertEquals(2, state.answeredCount)
    }

    @Test
    fun `TrueFalseUiState canGoNext is true when not at last`() {
        val statements = listOf(
            createStatement("1", "S1", true),
            createStatement("2", "S2", false)
        )
        
        val stateAt0 = TrueFalseUiState(statements = statements, currentIndex = 0)
        val stateAt1 = TrueFalseUiState(statements = statements, currentIndex = 1)
        
        assertTrue(stateAt0.canGoNext)
        assertFalse(stateAt1.canGoNext)
    }

    @Test
    fun `TrueFalseUiState canGoPrevious is true when not at first`() {
        val statements = listOf(
            createStatement("1", "S1", true),
            createStatement("2", "S2", false)
        )
        
        val stateAt0 = TrueFalseUiState(statements = statements, currentIndex = 0)
        val stateAt1 = TrueFalseUiState(statements = statements, currentIndex = 1)
        
        assertFalse(stateAt0.canGoPrevious)
        assertTrue(stateAt1.canGoPrevious)
    }

    @Test
    fun `TrueFalseUiState isLastStatement is correct`() {
        val statements = listOf(
            createStatement("1", "S1", true),
            createStatement("2", "S2", false)
        )
        
        val stateAt0 = TrueFalseUiState(statements = statements, currentIndex = 0)
        val stateAt1 = TrueFalseUiState(statements = statements, currentIndex = 1)
        
        assertFalse(stateAt0.isLastStatement)
        assertTrue(stateAt1.isLastStatement)
    }

    @Test
    fun `TrueFalseUiState isGenerating state transitions`() {
        val idle = TrueFalseUiState(isGenerating = false)
        val generating = idle.copy(isGenerating = true)
        val done = generating.copy(isGenerating = false)
        
        assertFalse(idle.isGenerating)
        assertTrue(generating.isGenerating)
        assertFalse(done.isGenerating)
    }

    @Test
    fun `TrueFalseUiState generationError can be set and cleared`() {
        val noError = TrueFalseUiState()
        val withError = noError.copy(generationError = "API key not configured")
        val errorCleared = withError.copy(generationError = null)
        
        assertNull(noError.generationError)
        assertEquals("API key not configured", withError.generationError)
        assertNull(errorCleared.generationError)
    }

    @Test
    fun `TrueFalseUiState sourceText for text import mode`() {
        val state = TrueFalseUiState()
        val updated = state.copy(sourceText = "Some pasted content for generating statements")
        
        assertEquals("", state.sourceText)
        assertEquals("Some pasted content for generating statements", updated.sourceText)
    }

    @Test
    fun `TrueFalseUiState sourceTextError can be set`() {
        val state = TrueFalseUiState()
        val withError = state.copy(sourceTextError = "Please enter some text")
        
        assertNull(state.sourceTextError)
        assertEquals("Please enter some text", withError.sourceTextError)
    }

    @Test
    fun `AnswerResult captures correct answer details`() {
        val result = AnswerResult(
            statementId = "stmt-1",
            userAnswer = true,
            isCorrect = true,
            explanation = "This is true because...",
            trapWords = listOf("always", "only"),
            upscTip = "Remember: Look for absolute qualifiers"
        )
        
        assertEquals("stmt-1", result.statementId)
        assertTrue(result.userAnswer!!)
        assertTrue(result.isCorrect)
        assertEquals("This is true because...", result.explanation)
        assertEquals(2, result.trapWords.size)
        assertTrue(result.trapWords.contains("always"))
    }

    @Test
    fun `AnswerResult handles skipped answer`() {
        val result = AnswerResult(
            statementId = "stmt-2",
            userAnswer = null,  // Skipped
            isCorrect = false,
            explanation = "Explanation text",
            trapWords = emptyList(),
            upscTip = "Tip text"
        )
        
        assertNull(result.userAnswer)
        assertFalse(result.isCorrect)
    }

    @Test
    fun `TrueFalseUiState result scores are tracked`() {
        val completedState = TrueFalseUiState(
            isCompleted = true,
            correctCount = 7,
            incorrectCount = 2,
            skippedCount = 1,
            finalScore = 12.34f
        )
        
        assertTrue(completedState.isCompleted)
        assertEquals(7, completedState.correctCount)
        assertEquals(2, completedState.incorrectCount)
        assertEquals(1, completedState.skippedCount)
        assertEquals(12.34f, completedState.finalScore, 0.01f)
    }

    @Test
    fun `TrueFalseUiState timer values are tracked`() {
        val state = TrueFalseUiState(
            sessionTimeElapsed = 120,
            currentStatementTime = 15
        )
        
        assertEquals(120, state.sessionTimeElapsed)
        assertEquals(15, state.currentStatementTime)
    }

    @Test
    fun `TrueFalseUiState showExplanation and lastAnswerResult`() {
        val result = AnswerResult(
            statementId = "1",
            userAnswer = false,
            isCorrect = false,
            explanation = "Wrong!",
            trapWords = emptyList(),
            upscTip = ""
        )
        
        val stateWithExplanation = TrueFalseUiState(
            showExplanation = true,
            lastAnswerResult = result
        )
        
        assertTrue(stateWithExplanation.showExplanation)
        assertNotNull(stateWithExplanation.lastAnswerResult)
        assertEquals("1", stateWithExplanation.lastAnswerResult?.statementId)
    }

    // Helper function to create TrueFalseStatement for tests
    private fun createStatement(
        id: String,
        statement: String,
        isTrue: Boolean
    ): TrueFalseStatement {
        return TrueFalseStatement(
            id = id,
            statement = statement,
            isTrue = isTrue,
            explanation = "Explanation for $statement",
            trapWords = emptyList(),
            upscTip = "Tip for $statement",
            difficulty = QuestionDifficulty.MEDIUM,
            subject = "Test Subject",
            topic = "Test Topic"
        )
    }
}
