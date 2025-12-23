package com.shivams.mockmate.ui.screens

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.Question
import com.shivams.mockmate.model.QuestionDifficulty
import com.shivams.mockmate.model.QuestionType
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.ui.components.ImportFromFileCard
import com.shivams.mockmate.ui.components.ImportFromPromptCard
import com.shivams.mockmate.ui.components.ImportHelpDialog
import com.shivams.mockmate.ui.components.ImportStatusView
import com.shivams.mockmate.ui.components.JsonFormatExample
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.SectionHeader
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestImportScreen(
    onNavigateBack: () -> Unit,
    onViewTests: () -> Unit,
    repository: TestRepository,
    initialMode: ImportMode = ImportMode.FileImport
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFileName by remember { mutableStateOf("No file selected") }
    var fileContent by remember { mutableStateOf<String?>(null) }
    var importStatus by remember { mutableStateOf<ImportStatus>(if (initialMode == ImportMode.Prompt) ImportStatus.PromptMode else ImportStatus.Ready) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        selectedFileName = c.getString(nameIndex)
                    }
                }
            }

            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(inputStream))
                fileContent = reader.readText()
                importStatus = ImportStatus.Ready
            } catch (e: Exception) {
                importStatus = ImportStatus.Error("Error reading file: ${e.message}")
            }
        }
    }

    fun processImport() {
        fileContent?.let {
            importStatus = ImportStatus.Processing
            coroutineScope.launch {
                try {
                    val test = parseJsonToTest(it)
                    repository.saveTest(test)
                    importStatus = ImportStatus.Success(test.name, test.questions.size)
                } catch (e: Exception) {
                    importStatus = ImportStatus.Error("Failed to import test: ${e.message}")
                }
            }
        } ?: run {
            importStatus = ImportStatus.Error("No file content to import")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MockMateTopBar(
            title = "Import Test Data",
            onBackClick = onNavigateBack,
            onHelpClick = { showHelpDialog = true }
        )

        if (showHelpDialog) {
            ImportHelpDialog(onDismiss = { showHelpDialog = false })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient Header Banner
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    androidx.compose.ui.graphics.Color(0xFF1E88E5),
                                    androidx.compose.ui.graphics.Color(0xFF7B1FA2)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "📥",
                                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        androidx.compose.foundation.layout.Column {
                            androidx.compose.material3.Text(
                                text = "Import JSON Tests",
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.material3.Text(
                                text = "Add custom mock tests from JSON files",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            if (importStatus != ImportStatus.PromptMode) {
                ImportFromFileCard(
                    selectedFileName = selectedFileName,
                    onSelectFileClick = { fileLauncher.launch("application/json") },
                    onImportClick = { processImport() },
                    onImportFromPromptClick = { importStatus = ImportStatus.PromptMode },
                    importStatus = importStatus,
                    fileContent = fileContent
                )
            }

            ImportStatusView(
                importStatus = importStatus,
                onImportAnother = {
                    selectedFileName = "No file selected"
                    fileContent = null
                    importStatus = ImportStatus.Ready
                },
                onViewTests = onViewTests,
                onTryAgain = { importStatus = ImportStatus.Ready }
            )

            if (importStatus == ImportStatus.PromptMode) {
                ImportFromPromptCard(
                    onCancel = { importStatus = ImportStatus.Ready },
                    onImport = {
                        fileContent = it
                        processImport()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(text = "JSON Format Example")

            JsonFormatExample()
        }
    }
}

private fun parseJsonToTest(jsonString: String): MockTest {
    val jsonObject = JSONObject(jsonString)

    val name = jsonObject.getString("name")
    val difficultyString = jsonObject.getString("difficulty")
    val timeLimit = jsonObject.getInt("timeLimit")
    val negativeMarking = jsonObject.optBoolean("negativeMarking", false)
    val negativeMarkingValue = jsonObject.optDouble("negativeMarkingValue", 0.33).toFloat()

    val questionsArray = jsonObject.getJSONArray("questions")
    val questions = List(questionsArray.length()) {
        val questionObj = questionsArray.getJSONObject(it)
        val optionsArray = questionObj.getJSONArray("options")
        val options = List(optionsArray.length()) { i -> optionsArray.getString(i) }

        Question(
            text = questionObj.getString("text"),
            options = options,
            correctOptionIndex = questionObj.getInt("correctOptionIndex"),
            explanation = questionObj.getString("explanation"),
            difficulty = QuestionDifficulty.valueOf(questionObj.optString("difficulty", "MEDIUM")),
            type = QuestionType.MULTIPLE_CHOICE,
            subject = questionObj.getString("subject"),
            topic = questionObj.getString("topic")
        )
    }

    return MockTest(
        name = name,
        difficulty = TestDifficulty.valueOf(difficultyString),
        questions = questions,
        timeLimit = timeLimit,
        negativeMarking = negativeMarking,
        negativeMarkingValue = negativeMarkingValue
    )
}

sealed class ImportStatus {
    object Ready : ImportStatus()
    object Processing : ImportStatus()
    data class Success(val testName: String, val questionCount: Int) : ImportStatus()
    data class Error(val message: String) : ImportStatus()
    object PromptMode : ImportStatus()
}

enum class ImportMode {
    FileImport,
    Prompt
}
