package com.shivams.mockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.database.AnalysisDao
import com.shivams.mockmate.data.database.AnalysisEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisHistoryViewModel @Inject constructor(
    private val analysisDao: AnalysisDao
) : ViewModel() {
    
    val allAnalyses = analysisDao.getAllAnalyses()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedAnalysis = MutableStateFlow<AnalysisEntity?>(null)
    val selectedAnalysis: StateFlow<AnalysisEntity?> = _selectedAnalysis.asStateFlow()
    
    fun loadAnalysis(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedAnalysis.value = analysisDao.getAnalysisById(id)
            _isLoading.value = false
        }
    }
    
    fun deleteAnalysis(id: Long) {
        viewModelScope.launch {
            analysisDao.deleteAnalysis(id)
        }
    }
    
    fun deleteAllAnalyses() {
        viewModelScope.launch {
            analysisDao.deleteAllAnalyses()
        }
    }
}
