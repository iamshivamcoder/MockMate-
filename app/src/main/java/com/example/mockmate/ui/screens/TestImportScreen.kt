package com.example.mockmate.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.MockTest
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.SectionHeader
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun TestImportScreen(
    onNavigateBack: () -> Unit,
    onViewTests: () -> Unit,
    repository: TestRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("No file selected") }
    var fileContent by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var importStatus by remember { mutableStateOf<ImportStatus?>(null) }
    var importedTest by remember { mutableStateOf<MockTest?>(null) }
    
    // File picker launcher
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            
            // Get file name
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        selectedFileName = c.getString(nameIndex)
                    }
                }
            }
            
            // Read file content
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                
                fileContent = stringBuilder.toString()
                importStatus = ImportStatus.Ready
            } catch (e: Exception) {
                importStatus = ImportStatus.Error("Error reading file: ${e.message}")
            }
        }
    }
    
    // Process and import the file
    fun processImport() {
        if (fileContent == null) {
            importStatus = ImportStatus.Error("No file content to import")
            return
        }
        
        isProcessing = true
        importStatus = ImportStatus.Processing
        
        coroutineScope.launch {
            try {
                // Parse JSON and import test
                val test = parseJsonToTest(fileContent!!)
                repository.saveTest(test)
                
                importedTest = test
                importStatus = ImportStatus.Success(
                    testName = test.name,
                    questionCount = test.questions.size
                )
            } catch (e: Exception) {
                importStatus = ImportStatus.Error(
                    "Failed to import test: ${e.message}"
                )
                e.printStackTrace()
            } finally {
                isProcessing = false
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MockMateTopBar(
            title = "Import Test Data",
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeader(text = "Import from JSON")
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select a JSON file containing test data",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // File selection area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "Upload File",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = selectedFileName,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = {
                                    fileLauncher.launch("application/json")
                                }
                            ) {
                                Text("Select JSON File")
                            }
                        }
                    }
                }
            }
            
            // Import status
            when (val status = importStatus) {
                is ImportStatus.Ready -> {
                    Button(
                        onClick = { processImport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        enabled = fileContent != null
                    ) {
                        Text("Import Test Data")
                    }
                }
                
                is ImportStatus.Processing -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Processing file...")
                    }
                }
                
                is ImportStatus.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Test Imported Successfully",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Name: ${status.testName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = "Questions: ${status.questionCount}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(onClick = {
                                    // Reset state for another import
                                    selectedFileUri = null
                                    selectedFileName = "No file selected"
                                    fileContent = null
                                    importStatus = null
                                    importedTest = null
                                }) {
                                    Text("Import Another")
                                }
                                
                                Button(onClick = onViewTests) {
                                    Text("View Tests")
                                }
                            }
                        }
                    }
                }
                
                is ImportStatus.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Import Failed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = status.message,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(onClick = {
                                // Reset for retry
                                importStatus = ImportStatus.Ready
                            }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
                
                else -> { /* Do nothing */ }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Example JSON format
            SectionHeader(text = "JSON Format Example")
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = """{
  "name": "UPSC Prelims 2023",
  "difficulty": "HARD",
  "timeLimit": 120,
  "negativeMarking": true,
  "negativeMarkingValue": 0.33,
  "questions": [
    {
      "text": "Which article deals with Right to Equality?",
      "options": ["Article 14", "Article 19", "Article 21", "Article 32"],
      "correctOptionIndex": 0,
      "explanation": "Article 14 provides equality before law.",
      "subject": "Indian Polity",
      "topic": "Constitution",
      "difficulty": "MEDIUM"
    },
    ...more questions
  ]
}""",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

// Helper function to parse JSON to MockTest
private suspend fun parseJsonToTest(jsonString: String): MockTest {
    val jsonObject = JSONObject(jsonString)
    
    // Parse basic test info
    val name = jsonObject.getString("name")
    val difficultyString = jsonObject.getString("difficulty")
    val timeLimit = jsonObject.getInt("timeLimit")
    
    // Parse optional fields with defaults
    val negativeMarking = if (jsonObject.has("negativeMarking")) {
        jsonObject.getBoolean("negativeMarking")
    } else {
        false
    }
    
    val negativeMarkingValue = if (jsonObject.has("negativeMarkingValue")) {
        jsonObject.getDouble("negativeMarkingValue").toFloat()
    } else {
        0.33f
    }
    
    // Parse questions
    val questionsArray = jsonObject.getJSONArray("questions")
    val questions = mutableListOf<com.example.mockmate.model.Question>()
    
    for (i in 0 until questionsArray.length()) {
        val questionObj = questionsArray.getJSONObject(i)
        
        // Parse options array
        val optionsArray = questionObj.getJSONArray("options")
        val options = mutableListOf<String>()
        for (j in 0 until optionsArray.length()) {
            options.add(optionsArray.getString(j))
        }
        
        // Create question object
        val question = com.example.mockmate.model.Question(
            text = questionObj.getString("text"),
            options = options,
            correctOptionIndex = questionObj.getInt("correctOptionIndex"),
            explanation = questionObj.getString("explanation"),
            difficulty = com.example.mockmate.model.QuestionDifficulty.valueOf(
                questionObj.optString("difficulty", "MEDIUM")
            ),
            type = com.example.mockmate.model.QuestionType.MULTIPLE_CHOICE,
            subject = questionObj.getString("subject"),
            topic = questionObj.getString("topic")
        )
        
        questions.add(question)
    }
    
    // Create and return the test
    return com.example.mockmate.model.MockTest(
        name = name,
        difficulty = com.example.mockmate.model.TestDifficulty.valueOf(difficultyString),
        questions = questions,
        timeLimit = timeLimit,
        negativeMarking = negativeMarking,
        negativeMarkingValue = negativeMarkingValue
    )
}

// Status states for import process
sealed class ImportStatus {
    object Ready : ImportStatus()
    object Processing : ImportStatus()
    data class Success(val testName: String, val questionCount: Int) : ImportStatus()
    data class Error(val message: String) : ImportStatus()
}