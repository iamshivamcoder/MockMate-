package com.shivams.mockmate.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.shivams.mockmate.data.database.AnalysisDao
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.data.repositories.UserProfileRepository
import com.shivams.mockmate.model.BackupData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val testRepository: TestRepository,
    private val userProfileRepository: UserProfileRepository,
    private val analysisDao: AnalysisDao,
    private val gson: Gson
) : ViewModel() {

    fun exportData(context: Context, uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Collect data
                val profile = userProfileRepository.getUserProfile().firstOrNull()
                val attempts = testRepository.getAllTestAttempts().first()
                val mockTests = testRepository.mockTests.first()
                val analysisReports = analysisDao.getAllAnalyses().first()

                val backupData = BackupData(
                    profile = profile,
                    attempts = attempts,
                    mockTests = mockTests,
                    analysisReports = analysisReports
                )

                val json = gson.toJson(backupData)

                // Write to URI
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(json)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    onResult(true, "Data exported successfully (${analysisReports.size} analysis reports)")
                }
            } catch (e: Exception) {
                Log.e("DataExport", "Error exporting data", e)
                withContext(Dispatchers.Main) {
                    onResult(false, e.message ?: "Unknown error during export")
                }
            }
        }
    }

    fun importData(context: Context, uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Read from URI
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        reader.readText()
                    }
                } ?: throw Exception("Could not read file")

                val backupData = gson.fromJson(json, BackupData::class.java)

                // Restore data
                // 1. Profile
                backupData.profile?.let {
                    userProfileRepository.saveUserProfile(it)
                }

                // 2. Mock Tests
                backupData.mockTests.forEach { test ->
                    testRepository.saveTest(test)
                }

                // 3. Test Attempts
                backupData.attempts.forEach { attempt ->
                    testRepository.saveTestAttempt(attempt)
                }
                
                // 4. Analysis Reports (NEW)
                backupData.analysisReports.forEach { analysis ->
                    analysisDao.insertAnalysis(analysis)
                }

                withContext(Dispatchers.Main) {
                    onResult(true, "Data imported successfully (${backupData.analysisReports.size} analysis reports)")
                }
            } catch (e: Exception) {
                Log.e("DataImport", "Error importing data", e)
                withContext(Dispatchers.Main) {
                    onResult(false, e.message ?: "Unknown error during import")
                }
            }
        }
    }
}
