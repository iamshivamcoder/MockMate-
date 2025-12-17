package com.shivams.mockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.domain.usecases.TestAttemptOperationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestHistoryViewModel @Inject constructor(
    private val testAttemptOperations: TestAttemptOperationsUseCase
) : ViewModel() {

    fun renameTestAttempt(attemptId: String, newName: String) {
        viewModelScope.launch {
            testAttemptOperations.renameTestAttempt(attemptId, newName)
        }
    }

    fun deleteTestAttempt(attemptId: String) {
        viewModelScope.launch {
            testAttemptOperations.deleteTestAttempt(attemptId)
        }
    }
}
