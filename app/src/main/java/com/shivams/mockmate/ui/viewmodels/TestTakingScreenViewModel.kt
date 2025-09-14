package com.shivams.mockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.QuestionStatus
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.UserAnswer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

data class TestTakingUiState(
    val isLoading: Boolean = true, // Added isLoading
    val mockTest: MockTest? = null,
    val currentQuestionIndex: Int = 0,
    val timeRemaining: Long = 0L,
    val selectedOptions: List<Int> = emptyList(),
    val questionStatus: List<QuestionStatus> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val questionTimeSpent: Map<Int, Int> = emptyMap()
)

class TestTakingScreenViewModel(
    private val repository: TestRepository,
    private val testId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestTakingUiState())
    val uiState: StateFlow<TestTakingUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadTest()
    }

    private fun loadTest() {
        _uiState.update { it.copy(isLoading = true) } // Set isLoading true at start
        viewModelScope.launch {
            try {
                val test = repository.getTestById(testId)
                test?.let {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false, // Set isLoading false on success
                            mockTest = it,
                            timeRemaining = it.timeLimit.toLong() * 60,
                            selectedOptions = List(it.questions.size) { -1 },
                            questionStatus = List(it.questions.size) { QuestionStatus.UNATTEMPTED }
                        )
                    }
                    startTimer(it.timeLimit.toLong() * 60)
                } ?: _uiState.update { // Handle test not found
                    it.copy(isLoading = false, errorMessage = "Test not found.")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load test: ${e.message}") } // Set isLoading false on error
            }
        }
    }

    private fun startTimer(initialTimeSeconds: Long) {
        timerJob?.cancel() // Cancel any existing timer
        timerJob = viewModelScope.launch {
            flow {
                for (i in initialTimeSeconds downTo 0) {
                    emit(i)
                    delay(1000)
                }
            }.collect { currentTime ->
                _uiState.update { it.copy(timeRemaining = currentTime) }
                if (currentTime == 0L) {
                    // Optionally auto-submit or handle timeout
                }
            }
        }
    }

    fun moveToNextQuestion() {
        _uiState.value.mockTest?.questions?.let {
            if (_uiState.value.currentQuestionIndex < it.size - 1) {
                _uiState.update { currentState ->
                    currentState.copy(currentQuestionIndex = currentState.currentQuestionIndex + 1)
                }
            }
        }
    }

    fun moveToPreviousQuestion() {
        if (_uiState.value.currentQuestionIndex > 0) {
            _uiState.update { currentState ->
                currentState.copy(currentQuestionIndex = currentState.currentQuestionIndex - 1)
            }
        }
    }

    fun updateSelectedOption(questionIndex: Int, optionIndex: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedOptions = currentState.selectedOptions.toMutableList().also {
                    if (questionIndex >= 0 && questionIndex < it.size) {
                        it[questionIndex] = optionIndex
                    }
                }
            )
        }
    }

    fun updateQuestionStatus(questionIndex: Int, status: QuestionStatus) {
        _uiState.update { currentState ->
            currentState.copy(
                questionStatus = currentState.questionStatus.toMutableList().also {
                     if (questionIndex >= 0 && questionIndex < it.size) {
                        it[questionIndex] = status
                    }
                }
            )
        }
    }

    fun toggleBookmark(questionIndex: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                questionStatus = currentState.questionStatus.toMutableList().also {
                    if (questionIndex >= 0 && questionIndex < it.size) {
                        it[questionIndex] = when (it[questionIndex]) {
                            QuestionStatus.BOOKMARKED -> QuestionStatus.UNATTEMPTED // Or original status if stored
                            else -> QuestionStatus.BOOKMARKED
                        }
                    }
                }
            )
        }
    }

    fun toggleMarkForReview(questionIndex: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                questionStatus = currentState.questionStatus.toMutableList().also {
                    if (questionIndex >= 0 && questionIndex < it.size) {
                         it[questionIndex] = when (it[questionIndex]) {
                            QuestionStatus.MARKED_FOR_REVIEW -> QuestionStatus.UNATTEMPTED // Or original status
                            else -> QuestionStatus.MARKED_FOR_REVIEW
                        }
                    }
                }
            )
        }
    }

    fun updateQuestionTimeSpent(questionIndex: Int, timeSpent: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                questionTimeSpent = currentState.questionTimeSpent.toMutableMap().apply {
                    put(questionIndex, (this[questionIndex] ?: 0) + timeSpent)
                }
            )
        }
    }

    fun submitTestAttempt(onSuccess: (String) -> Unit) {
        if (_uiState.value.isSaving) return
        if (_uiState.value.mockTest == null) {
            _uiState.update { it.copy(errorMessage = "Test data not available") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        timerJob?.cancel()

        viewModelScope.launch {
            try {
                val currentMockTest = _uiState.value.mockTest!!
                val endTime = System.currentTimeMillis()
                val elapsedTimeSeconds = (currentMockTest.timeLimit * 60) - _uiState.value.timeRemaining
                val startTime = endTime - (elapsedTimeSeconds * 1000)

                val userAnswers = mutableMapOf<String, UserAnswer>()
                currentMockTest.questions.forEachIndexed { index, question ->
                     if (index < _uiState.value.selectedOptions.size && index < _uiState.value.questionStatus.size) {
                        userAnswers[question.id] = UserAnswer(
                            questionId = question.id,
                            selectedOptionIndex = _uiState.value.selectedOptions[index].takeIf { it != -1 },
                            timeSpent = _uiState.value.questionTimeSpent[index] ?: 0,
                            status = _uiState.value.questionStatus[index]
                        )
                    }
                }
                
                val testAttempt = TestAttempt(
                    id = UUID.randomUUID().toString(),
                    testId = testId,
                    startTime = Date(startTime),
                    endTime = Date(endTime),
                    userAnswers = userAnswers,
                    isCompleted = true
                )
                
                repository.saveTestAttempt(testAttempt)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess(testAttempt.id)

            } catch (e: CancellationException) {
                _uiState.update { it.copy(isSaving = false) }
                throw e
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        errorMessage = when (e) {
                            is IllegalStateException -> e.message ?: "Invalid test state"
                            is IllegalArgumentException -> e.message ?: "Invalid test data"
                            else -> "Failed to submit test: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
                        }
                    )
                }
                android.util.Log.e("TestTakingViewModel", "Error submitting test", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun provideFactory(
            repository: TestRepository,
            testId: String
        ): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TestTakingScreenViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return TestTakingScreenViewModel(repository, testId) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
