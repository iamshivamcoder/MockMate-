package com.shivams.mockmate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.service.AiInsightsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for AI Test Generator
 */
data class AiTestGeneratorUiState(
    val topic: String = "",
    val subject: String = "History",
    val difficulty: TestDifficulty = TestDifficulty.MEDIUM,
    val numberOfQuestions: Int = 10,
    val timeLimit: Int = 15,
    val negativeMarking: Boolean = true,
    val negativeMarkingValue: Float = 0.25f,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val topicError: String? = null
)

/**
 * ViewModel for AI Test Generation
 */
@HiltViewModel
class AiTestGeneratorViewModel @Inject constructor(
    private val aiInsightsService: AiInsightsService,
    private val testRepository: TestRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AiTestGeneratorVM"
    }

    private val _uiState = MutableStateFlow(AiTestGeneratorUiState())
    val uiState: StateFlow<AiTestGeneratorUiState> = _uiState.asStateFlow()

    fun updateTopic(topic: String) {
        _uiState.update { it.copy(topic = topic, topicError = null) }
    }

    fun updateSubject(subject: String) {
        _uiState.update { it.copy(subject = subject) }
    }

    fun updateDifficulty(difficulty: TestDifficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun updateNumberOfQuestions(count: Int) {
        _uiState.update { it.copy(numberOfQuestions = count.coerceIn(5, 25)) }
    }

    fun updateTimeLimit(minutes: Int) {
        _uiState.update { it.copy(timeLimit = minutes.coerceIn(5, 120)) }
    }

    fun updateNegativeMarking(enabled: Boolean) {
        _uiState.update { it.copy(negativeMarking = enabled) }
    }

    fun updateNegativeMarkingValue(value: Float) {
        _uiState.update { it.copy(negativeMarkingValue = value) }
    }

    fun generateTest(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        
        if (state.topic.isBlank()) {
            _uiState.update { it.copy(topicError = "Please enter a topic") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            
            try {
                val result = aiInsightsService.generateTest(
                    topic = state.topic,
                    subject = state.subject,
                    difficulty = state.difficulty,
                    numberOfQuestions = state.numberOfQuestions,
                    timeLimit = state.timeLimit,
                    negativeMarking = state.negativeMarking,
                    negativeMarkingValue = state.negativeMarkingValue
                )
                
                result.fold(
                    onSuccess = { test ->
                        // Save the test
                        testRepository.saveTest(test)
                        Log.d(TAG, "Test generated and saved: ${test.id}")
                        _uiState.update { it.copy(isGenerating = false) }
                        onSuccess(test.id)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to generate test", error)
                        _uiState.update { 
                            it.copy(
                                isGenerating = false, 
                                error = error.message ?: "Failed to generate test"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during test generation", e)
                _uiState.update { 
                    it.copy(
                        isGenerating = false, 
                        error = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
