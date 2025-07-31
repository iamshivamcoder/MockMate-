package com.example.mockmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.TestAttempt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TestResultUiState(
    val isLoading: Boolean = true,
    val mockTest: MockTest? = null,
    val testAttempt: TestAttempt? = null,
    val error: String? = null,
    val isDeleted: Boolean = false, // Flag for deletion status
    val updatedCustomName: String? = null
)

class TestResultViewModel(
    private val testRepository: TestRepository,
    private val testId: String,
    private val attemptId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestResultUiState())
    val uiState: StateFlow<TestResultUiState> = _uiState.asStateFlow()

    init {
        loadTestResult()
    }

    fun loadTestResult() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val test = testRepository.getTestById(testId)
                val attempt = testRepository.getTestAttemptById(attemptId)
                _uiState.update {
                    it.copy(isLoading = false, mockTest = test, testAttempt = attempt, error = null)
                }
                if (test == null || attempt == null) {
                     _uiState.update { it.copy(isLoading = false, error = "Test or Attempt not found.") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "An unknown error occurred")
                }
            }
        }
    }

    /**
     * Deletes the current test attempt and updates the UI state.
     */
    fun deleteTestAttempt() {
        viewModelScope.launch {
            try {
                testRepository.deleteTestAttempt(attemptId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete attempt")
                }
            }
        }
    }

    fun renameTestAttempt(customName: String) {
        viewModelScope.launch {
            try {
                testRepository.updateTestAttemptCustomName(attemptId, customName)
                _uiState.update { it.copy(updatedCustomName = customName) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to rename attempt")
                }
            }
        }
    }
}
