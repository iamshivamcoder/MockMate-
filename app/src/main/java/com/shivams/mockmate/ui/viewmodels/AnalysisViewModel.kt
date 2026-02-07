package com.shivams.mockmate.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivams.mockmate.data.repositories.PdfAnalysisRepository
import com.shivams.mockmate.model.analysis.AnalysisReport
import com.shivams.mockmate.model.analysis.CognitiveTag
import com.shivams.mockmate.model.analysis.QuestionAnalysis
import com.shivams.mockmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * UI State for the Analysis Dashboard
 */
data class AnalysisUiState(
    val isLoading: Boolean = false,
    val report: AnalysisReport? = null,
    val error: String? = null,
    val selectedPdfUri: Uri? = null
) {
    val hasReport: Boolean get() = report != null
    val showError: Boolean get() = error != null && !isLoading
}

/**
 * One-time UI events for snackbars, navigation, etc.
 */
sealed class AnalysisUiEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean = true) : AnalysisUiEvent()
    data object AnalysisComplete : AnalysisUiEvent()
}

/**
 * ViewModel for the Metacognitive PDF Analyzer feature.
 * Bridges the Repository to the UI and provides cognitive filtering functions.
 */
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: PdfAnalysisRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()
    
    // Channel for one-time UI events (snackbars, etc.)
    private val _uiEvent = Channel<AnalysisUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    
    /**
     * Analyze a PDF from the given content URI.
     * Call this after user selects a file or shares a PDF to the app.
     */
    fun analyzePdf(uri: Uri) {
        _uiState.update { it.copy(selectedPdfUri = uri, error = null) }
        
        viewModelScope.launch {
            repository.uploadAndAnalyze(uri).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                report = resource.data, 
                                error = null
                            ) 
                        }
                        _uiEvent.send(AnalysisUiEvent.AnalysisComplete)
                        _uiEvent.send(AnalysisUiEvent.ShowSnackbar(
                            message = "✅ Analysis complete!",
                            isError = false
                        ))
                    }
                    is Resource.Error -> {
                        // Log the error for debugging
                        android.util.Log.e("AnalysisError", "Analysis failed - Reason: ${resource.message}", resource.exception)
                        
                        val userFriendlyMessage = mapErrorToUserMessage(resource.message, resource.exception)
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = userFriendlyMessage
                            ) 
                        }
                        _uiEvent.send(AnalysisUiEvent.ShowSnackbar(
                            message = userFriendlyMessage,
                            isError = true
                        ))
                    }
                }
            }
        }
    }
    
    /**
     * Map technical errors to user-friendly messages.
     */
    private fun mapErrorToUserMessage(message: String?, exception: Throwable?): String {
        return when {
            // Timeout errors (common with Render cold starts)
            exception is SocketTimeoutException || 
            message?.contains("timeout", ignoreCase = true) == true -> 
                "⏳ Server is waking up. Please try again in 1 minute."
            
            // Network connectivity issues
            exception is UnknownHostException ||
            message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                "📶 No internet connection. Please check your network."
            
            // Payload too large
            message?.contains("413", ignoreCase = true) == true ||
            message?.contains("too large", ignoreCase = true) == true ->
                "📄 PDF is too large. Please upload fewer pages (max ~20 pages)."
            
            // Server errors
            message?.contains("500", ignoreCase = true) == true ->
                "🔧 Server error. Our team is on it. Try again later."
            
            // Rate limiting
            message?.contains("429", ignoreCase = true) == true ->
                "🚦 Too many requests. Please wait a moment."
            
            // Generic fallback
            else -> "❌ Analysis failed. Please check your connection and try again."
        }
    }
    
    /**
     * Try to restore the last cached analysis report.
     */
    fun restoreLastReport() {
        viewModelScope.launch {
            val cachedReport = repository.getLastAnalysisReport()
            if (cachedReport != null) {
                _uiState.update { it.copy(report = cachedReport) }
            }
        }
    }
    
    /**
     * Clear the current analysis and reset state.
     */
    fun clearAnalysis() {
        viewModelScope.launch {
            repository.clearCache()
            _uiState.update { 
                AnalysisUiState() 
            }
        }
    }
    
    /**
     * Dismiss the current error message.
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // ==================== Cognitive Filtering Functions ====================
    
    /**
     * Get questions tagged as CONCEPT_COLLAPSE.
     * These show high effort but wrong answer - needs fundamental concept review.
     */
    fun getConceptCollapseQuestions(): List<QuestionAnalysis> {
        return _uiState.value.report?.questions?.filter { 
            it.cognitiveTag == CognitiveTag.CONCEPT_COLLAPSE 
        } ?: emptyList()
    }
    
    /**
     * Get questions tagged as FLUKE (silly mistakes).
     * These were answered quickly without thought and were wrong.
     */
    fun getSillyMistakes(): List<QuestionAnalysis> {
        return _uiState.value.report?.questions?.filter { 
            it.cognitiveTag == CognitiveTag.FLUKE 
        } ?: emptyList()
    }
    
    /**
     * Get questions tagged as SOLID.
     * These show confident, correct answers with good methodology.
     */
    fun getSolidKnowledge(): List<QuestionAnalysis> {
        return _uiState.value.report?.questions?.filter { 
            it.cognitiveTag == CognitiveTag.SOLID 
        } ?: emptyList()
    }
    
    /**
     * Get questions tagged as DOUBT.
     * These had brown ink markers indicating uncertainty.
     */
    fun getDoubtQuestions(): List<QuestionAnalysis> {
        return _uiState.value.report?.questions?.filter { 
            it.cognitiveTag == CognitiveTag.DOUBT 
        } ?: emptyList()
    }
    
    /**
     * Get questions tagged as INTUITION.
     * These were quick correct answers showing good instinct.
     */
    fun getIntuitionQuestions(): List<QuestionAnalysis> {
        return _uiState.value.report?.questions?.filter { 
            it.cognitiveTag == CognitiveTag.INTUITION 
        } ?: emptyList()
    }
    
    /**
     * Get all questions that need immediate review.
     * Combines CONCEPT_COLLAPSE and DOUBT tags.
     */
    fun getImmediateActionQuestions(): List<QuestionAnalysis> {
        return _uiState.value.report?.questions?.filter { 
            it.needsReview 
        } ?: emptyList()
    }
}
