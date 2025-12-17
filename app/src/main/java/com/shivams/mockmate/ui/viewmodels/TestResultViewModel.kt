package com.shivams.mockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.domain.usecases.TestAttemptOperationsUseCase
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
    private val testAttemptOperations: TestAttemptOperationsUseCase,
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
            val result = testAttemptOperations.deleteTestAttempt(attemptId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isDeleted = true) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to delete attempt")
                    }
                }
            )
        }
    }

    fun renameTestAttempt(customName: String) {
        viewModelScope.launch {
            _uiState.value.testAttempt?.let { currentAttempt ->
                val result = testAttemptOperations.renameTestAttempt(attemptId, customName)
                result.fold(
                    onSuccess = {
                        val updatedAttempt = currentAttempt.copy(customName = customName)
                        _uiState.update {
                            it.copy(
                                testAttempt = updatedAttempt,
                                updatedCustomName = customName,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(error = exception.message ?: "Failed to rename attempt")
                        }
                    }
                )
            } ?: run {
                _uiState.update {
                    it.copy(error = "Cannot rename, attempt data is missing.")
                }
            }
        }
    }
}
