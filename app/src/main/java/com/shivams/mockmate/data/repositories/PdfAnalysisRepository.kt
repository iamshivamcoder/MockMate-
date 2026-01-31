package com.shivams.mockmate.data.repositories

import android.net.Uri
import com.shivams.mockmate.model.analysis.AnalysisReport
import com.shivams.mockmate.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for PDF analysis operations.
 * Handles uploading annotated PDFs and receiving cognitive analysis results.
 */
interface PdfAnalysisRepository {
    
    /**
     * Upload a PDF file for analysis and receive the cognitive diagnosis report.
     *
     * @param contentUri The content:// URI from File Picker or Share Sheet
     * @return A Flow emitting Resource states: Loading → Success/Error
     */
    fun uploadAndAnalyze(contentUri: Uri): Flow<Resource<AnalysisReport>>
    
    /**
     * Get the last analysis report if available.
     * Useful for restoring state after process death.
     *
     * @return The cached analysis report, or null if none exists
     */
    suspend fun getLastAnalysisReport(): AnalysisReport?
    
    /**
     * Clear any cached analysis data.
     */
    suspend fun clearCache()
}
