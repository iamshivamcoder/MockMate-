package com.shivams.mockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.TestRepository
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.TestAttempt
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

                if (test == null || attempt == null) {
                     _uiState.update { it.copy(isLoading = false, error = "Test or Attempt not found.") }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, mockTest = test, testAttempt = attempt, error = null)
                    }
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
            _uiState.value.testAttempt?.let { currentAttempt -> // Get the current attempt
                try {
                    testRepository.updateTestAttemptCustomName(attemptId, customName)
                    // Update the testAttempt object in the uiState with the new customName
                    val updatedAttempt = currentAttempt.copy(customName = customName)
                    _uiState.update {
                        it.copy(
                            testAttempt = updatedAttempt,
                            updatedCustomName = customName, // You can keep this if used elsewhere
                            error = null // Clear any previous error
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to rename attempt")
                    }
                }
            } ?: run {
                 // Handle case where currentAttempt is null, though unlikely if rename is attempted
                _uiState.update {
                    it.copy(error = "Cannot rename, attempt data is missing.")
                }
            }
        }
    }
}
