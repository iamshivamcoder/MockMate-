package com.example.mockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.QuestionStatus
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.UserAnswer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class TestTakingScreenViewModel(
    private val repository: TestRepository,
    private val testId: String
) : ViewModel() {

    private val _mockTest = MutableStateFlow<MockTest?>(null)
    val mockTest: StateFlow<MockTest?> = _mockTest.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _timeRemaining = MutableStateFlow(0L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    private val _selectedOptions = MutableStateFlow<List<Int>>(emptyList())
    val selectedOptions: StateFlow<List<Int>> = _selectedOptions.asStateFlow()

    private val _questionStatus = MutableStateFlow<List<QuestionStatus>>(emptyList())
    val questionStatus: StateFlow<List<QuestionStatus>> = _questionStatus.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _questionTimeSpent = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val questionTimeSpent: StateFlow<Map<Int, Int>> = _questionTimeSpent.asStateFlow()

    init {
        loadTest()
    }

    private fun loadTest() {
        viewModelScope.launch {
            try {
                val test = repository.getTestById(testId)
                test?.let {
                    _mockTest.value = it
                    _timeRemaining.value = it.timeLimit.toLong() * 60
                    _selectedOptions.value = List(it.questions.size) { -1 }
                    _questionStatus.value = List(it.questions.size) { QuestionStatus.UNATTEMPTED }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load test: ${e.message}"
            }
        }
    }

    fun updateTimeRemaining(newTime: Long) {
        _timeRemaining.value = newTime
    }

    fun updateSelectedOption(questionIndex: Int, optionIndex: Int) {
        _selectedOptions.value = _selectedOptions.value.toMutableList().also {
            it[questionIndex] = optionIndex
        }
    }

    fun updateQuestionStatus(questionIndex: Int, status: QuestionStatus) {
        _questionStatus.value = _questionStatus.value.toMutableList().also {
            it[questionIndex] = status
        }
    }

    fun toggleBookmark(questionIndex: Int) {
        _questionStatus.value = _questionStatus.value.toMutableList().also {
            it[questionIndex] = when (it[questionIndex]) {
                QuestionStatus.BOOKMARKED -> QuestionStatus.UNATTEMPTED
                else -> QuestionStatus.BOOKMARKED
            }
        }
    }

    fun toggleMarkForReview(questionIndex: Int) {
        _questionStatus.value = _questionStatus.value.toMutableList().also {
            it[questionIndex] = when (it[questionIndex]) {
                QuestionStatus.MARKED_FOR_REVIEW -> QuestionStatus.UNATTEMPTED
                else -> QuestionStatus.MARKED_FOR_REVIEW
            }
        }
    }

    fun updateQuestionTimeSpent(questionIndex: Int, timeSpent: Int) {
        _questionTimeSpent.value = _questionTimeSpent.value.toMutableMap().apply {
            put(questionIndex, (this[questionIndex] ?: 0) + timeSpent)
        }
    }

    fun submitTestAttempt(onSuccess: (String) -> Unit) {
        if (_isSaving.value) return  // Prevent multiple submissions
        
        if (_mockTest.value == null) {
            _errorMessage.value = "Test data not available"
            return
        }

        _isSaving.value = true
        _errorMessage.value = null // Clear previous error

        viewModelScope.launch {
            try {
                // Create test attempt with current time
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (_mockTest.value!!.timeLimit * 60 * 1000)
                
                val userAnswers = mutableMapOf<String, UserAnswer>()
                _mockTest.value!!.questions.forEachIndexed { index, question ->
                    userAnswers[question.id] = UserAnswer(
                        questionId = question.id,
                        selectedOptionIndex = _selectedOptions.value[index].takeIf { it != -1 },
                        timeSpent = _questionTimeSpent.value[index] ?: 0,
                        status = _questionStatus.value[index]
                    )
                }
                
                val testAttempt = TestAttempt(
                    id = UUID.randomUUID().toString(),
                    testId = testId,
                    startTime = Date(startTime),
                    endTime = Date(endTime),
                    userAnswers = userAnswers,
                    isCompleted = true
                )
                
                // Save to repository
                repository.saveTestAttempt(testAttempt)

                // Success - reset state and notify
                _isSaving.value = false
                onSuccess(testAttempt.id)

            } catch (e: CancellationException) {
                // Don't handle cancellation as error
                _isSaving.value = false
                throw e
            } catch (e: Exception) {
                // Handle all other exceptions
                _isSaving.value = false
                _errorMessage.value = when (e) {
                    is IllegalStateException -> e.message ?: "Invalid test state"
                    is IllegalArgumentException -> e.message ?: "Invalid test data"
                    else -> "Failed to submit test: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
                }
                android.util.Log.e("TestTakingViewModel", "Error submitting test", e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
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
