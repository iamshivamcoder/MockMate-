package com.shivams.mockmate.data.repositories

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.gson.Gson
import com.shivams.mockmate.data.database.AnalysisDao
import com.shivams.mockmate.data.database.AnalysisEntity
import com.shivams.mockmate.data.remote.AnalysisApi
import com.shivams.mockmate.model.analysis.AnalysisReport
import com.shivams.mockmate.model.analysis.CognitiveTag
import com.shivams.mockmate.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real implementation of PdfAnalysisRepository that connects to the FastAPI backend
 * and saves results to local database.
 */
@Singleton
class RealPdfAnalysisRepository @Inject constructor(
    private val api: AnalysisApi,
    private val analysisDao: AnalysisDao,
    @ApplicationContext private val context: Context
) : PdfAnalysisRepository {
    
    private val gson = Gson()
    
    companion object {
        private const val TAG = "RealPdfAnalysisRepo"
    }
    
    private var cachedReport: AnalysisReport? = null
    private var currentFileName: String = ""
    
    override fun uploadAndAnalyze(contentUri: Uri): Flow<Resource<AnalysisReport>> = flow {
        emit(Resource.Loading)
        
        try {
            // Convert content:// URI to a temporary file
            val tempFile = uriToTempFile(contentUri)
            
            if (tempFile == null) {
                emit(Resource.Error("Failed to read PDF file"))
                return@flow
            }
            
            currentFileName = tempFile.name
            Log.d(TAG, "Uploading PDF: ${tempFile.name} (${tempFile.length() / 1024} KB)")
            
            // Create multipart request body
            val requestBody = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                name = "file",
                filename = tempFile.name,
                body = requestBody
            )
            
            // Make API call
            val response = api.uploadPdf(multipartBody)
            
            // Clean up temp file
            tempFile.delete()
            
            if (response.isSuccessful && response.body() != null) {
                val analysisResponse = response.body()!!
                
                // Generate mentor feedback
                val mentorFeedback = generateMentorFeedback(analysisResponse)
                val report = AnalysisReport.fromResponse(analysisResponse, mentorFeedback)
                
                // Save to database
                saveToDatabase(report, currentFileName)
                
                cachedReport = report
                emit(Resource.Success(report))
                
                Log.d(TAG, "Analysis complete: ${report.totalQuestions} questions analyzed")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "API error: ${response.code()} - $errorMsg")
                emit(Resource.Error("Analysis failed: ${response.code()}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during analysis", e)
            emit(Resource.Error("Network error: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun getLastAnalysisReport(): AnalysisReport? = cachedReport
    
    override suspend fun clearCache() {
        cachedReport = null
    }
    
    /**
     * Save analysis report to local database for history.
     */
    private suspend fun saveToDatabase(report: AnalysisReport, fileName: String) {
        try {
            val entity = AnalysisEntity(
                testSubject = report.testSubject,
                totalQuestions = report.totalQuestions,
                analysisTimestamp = report.analysisTimestamp,
                mentorFeedback = report.mentorFeedback,
                solidCount = report.summary.solidCount,
                conceptCollapseCount = report.summary.conceptCollapseCount,
                intuitionCount = report.summary.intuitionCount,
                flukeCount = report.summary.flukeCount,
                doubtCount = report.summary.doubtCount,
                correctCount = report.summary.correctCount,
                overallAccuracy = report.summary.overallAccuracy,
                questionsJson = gson.toJson(report.questions),
                pdfFileName = fileName
            )
            
            val id = analysisDao.insertAnalysis(entity)
            Log.d(TAG, "Saved analysis to database with id: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save analysis to database", e)
        }
    }
    
    /**
     * Convert a content:// URI to a temporary file that can be uploaded.
     */
    private fun uriToTempFile(uri: Uri): File? {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            
            // Get the file name from the URI
            val fileName = getFileName(contentResolver, uri) ?: "upload.pdf"
            
            // Create temp file in cache directory
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}_$fileName")
            
            // Copy content to temp file
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert URI to file", e)
            null
        }
    }
    
    /**
     * Get the original file name from a content:// URI.
     */
    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
    
    /**
     * Generate mentor feedback based on the analysis results.
     */
    private fun generateMentorFeedback(response: com.shivams.mockmate.model.analysis.AnalysisResponse): String {
        val questions = response.questions
        val total = questions.size
        val correct = questions.count { it.aiVerdict.isCorrect }
        val accuracy = if (total > 0) (correct * 100 / total) else 0
        
        val conceptCollapseCount = questions.count { 
            it.aiVerdict.cognitiveTag == CognitiveTag.CONCEPT_COLLAPSE 
        }
        val flukeCount = questions.count { 
            it.aiVerdict.cognitiveTag == CognitiveTag.FLUKE 
        }
        val doubtCount = questions.count { 
            it.aiVerdict.cognitiveTag == CognitiveTag.DOUBT 
        }
        
        return buildString {
            appendLine("📊 **Analysis Summary**")
            appendLine()
            appendLine("Overall accuracy: $accuracy% ($correct/$total)")
            appendLine()
            
            if (conceptCollapseCount > 0) {
                appendLine("⚠️ **Concept Collapse** detected in $conceptCollapseCount question(s).")
                appendLine("These need fundamental revision - you put in effort but had wrong understanding.")
                appendLine()
            }
            
            if (flukeCount > 0) {
                appendLine("⏱️ **Silly Mistakes**: $flukeCount quick wrong answer(s).")
                appendLine("Slow down and read questions carefully!")
                appendLine()
            }
            
            if (doubtCount > 0) {
                appendLine("🤔 **Self-doubt** markers in $doubtCount question(s).")
                appendLine("Your brown ink shows awareness - keep building confidence!")
            }
            
            if (conceptCollapseCount == 0 && flukeCount == 0) {
                appendLine("✨ Great job! No major issues detected.")
            }
        }.trim()
    }
}
