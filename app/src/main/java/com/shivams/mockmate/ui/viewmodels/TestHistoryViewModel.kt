package com.shivams.mockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.repositories.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestHistoryViewModel @Inject constructor(
    private val repository: TestRepository
) : ViewModel() {

    fun renameTestAttempt(attemptId: String, newName: String) {
        viewModelScope.launch {
            repository.updateTestAttemptCustomName(attemptId, newName)
        }
    }

    fun deleteTestAttempt(attemptId: String) {
        viewModelScope.launch {
            repository.deleteTestAttempt(attemptId)
        }
    }
}
