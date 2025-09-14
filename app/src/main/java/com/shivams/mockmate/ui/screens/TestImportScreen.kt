package com.shivams.mockmate.ui.screens

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.Question
import com.shivams.mockmate.model.QuestionDifficulty
import com.shivams.mockmate.model.QuestionType
import com.shivams.mockmate.model.TestDifficulty
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
    
    // State
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) } // Variable is used, warning is a false positive
    var selectedFileName by remember { mutableStateOf("No file selected") }
    var fileContent by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) } // Variable is used, warning is a false positive
    var importStatus by remember { mutableStateOf<ImportStatus?>(if (initialMode == ImportMode.Prompt) ImportStatus.PromptMode else ImportStatus.Ready) }
    var importedTest by remember { mutableStateOf<MockTest?>(null) } // Variable is used, warning is a false positive
    
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
                e.printStackTrace() // It's fine to keep this for debugging failed imports
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Import buttons directly inside the card for better visibility
                    if (importStatus is ImportStatus.Ready) {
                        Button(
                            onClick = { processImport() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            enabled = fileContent != null
                        ) {
                            Text("Import Test Data")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { importStatus = ImportStatus.PromptMode },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text("Import from Prompt")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Scroll down for more options if needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Import status
            when (val status = importStatus) {
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
                                    // selectedFileUri = null // This was unused after assignment
                                    selectedFileName = "No file selected"
                                    fileContent = null
                                    importStatus = ImportStatus.Ready // Set to ready instead of null
                                    // importedTest = null // This was unused after assignment
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
                
                is ImportStatus.PromptMode -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "Use this prompt to generate UPSC CSE level test data with an LLM:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "I am preparing a mock test generator for UPSC Civil Services Examination (CSE) preparation. Before generating the test in JSON format, I need to gather specific requirements. Please ask the following questions to customize the mock test:\n1. Which specific subject areas or topics do you want to focus on (e.g., History, Geography, Polity, Economy, Environment, Science & Technology, Current Affairs)?\n2. How many questions would you like in the test?\n3. What should be the difficulty level of the questions (EASY, MEDIUM, HARD, or a mix)?\nAfter receiving my answers to these questions, confirm the details with me. Only after my confirmation, generate the mock test in JSON format. The JSON should include test name, difficulty (EASY, MEDIUM, HARD), time limit in minutes, negative marking (true/false), negative marking value (if applicable), and an array of questions. Each question must have text, an array of 4 options, correct option index (0-3), detailed explanation, subject, topic within the subject, and difficulty (EASY, MEDIUM, HARD). Ensure questions are of UPSC CSE standard, covering analytical and conceptual understanding. Format the JSON exactly as shown in the example below to ensure compatibility with the app:\n\n{\n  \"name\": \"UPSC CSE Prelims Mock Test 1\",\n  \"difficulty\": \"HARD\",\n  \"timeLimit\": 120,\n  \"negativeMarking\": true,\n  \"negativeMarkingValue\": 0.66,\n  \"questions\": [\n    {\n      \"text\": \"Which of the following is a fundamental duty under the Indian Constitution?\",\n      \"options\": [\"To vote in elections\", \"To pay taxes\", \"To safeguard public property\", \"To follow traffic rules\"],\n      \"correctOptionIndex\": 2,\n      \"explanation\": \"Article 51A of the Indian Constitution lists fundamental duties, including the duty to safeguard public property and to abjure violence.\",\n      \"subject\": \"Indian Polity\",\n      \"topic\": \"Constitution\",\n      \"difficulty\": \"MEDIUM\"\n    }\n    // Add more questions here\n  ]\n}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Prompt", "I am preparing a mock test generator for UPSC Civil Services Examination (CSE) preparation. Before generating the test in JSON format, I need to gather specific requirements. Please ask the following questions to customize the mock test:\n1. Which specific subject areas or topics do you want to focus on (e.g., History, Geography, Polity, Economy, Environment, Science & Technology, Current Affairs)?\n2. How many questions would you like in the test?\n3. What should be the difficulty level of the questions (EASY, MEDIUM, HARD, or a mix)?\nAfter receiving my answers to these questions, confirm the details with me. Only after my confirmation, generate the mock test in JSON format. The JSON should include test name, difficulty (EASY, MEDIUM, HARD), time limit in minutes, negative marking (true/false), negative marking value (if applicable), and an array of questions. Each question must have text, an array of 4 options, correct option index (0-3), detailed explanation, subject, topic within the subject, and difficulty (EASY, MEDIUM, HARD). Ensure questions are of UPSC CSE standard, covering analytical and conceptual understanding. Format the JSON exactly as shown in the example below to ensure compatibility with the app:\n\n{\n  \"name\": \"UPSC CSE Prelims Mock Test 1\",\n  \"difficulty\": \"HARD\",\n  \"timeLimit\": 120,\n  \"negativeMarking\": true,\n  \"negativeMarkingValue\": 0.66,\n  \"questions\": [\n    {\n      \"text\": \"Which of the following is a fundamental duty under the Indian Constitution?\",\n      \"options\": [\"To vote in elections\", \"To pay taxes\", \"To safeguard public property\", \"To follow traffic rules\"],\n      \"correctOptionIndex\": 2,\n      \"explanation\": \"Article 51A of the Indian Constitution lists fundamental duties, including the duty to safeguard public property and to abjure violence.\",\n      \"subject\": \"Indian Polity\",\n      \"topic\": \"Constitution\",\n      \"difficulty\": \"MEDIUM\"\n    }\n    // Add more questions here\n  ]\n}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Prompt copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Copy Prompt")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Paste the JSON response from the LLM below:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        var pastedResponse by remember { mutableStateOf("") }
                        androidx.compose.material3.OutlinedTextField(
                            value = pastedResponse,
                            onValueChange = { pastedResponse = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            placeholder = { Text("Paste JSON response here") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(onClick = { importStatus = ImportStatus.Ready }) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (pastedResponse.isNotEmpty()) {
                                        fileContent = pastedResponse
                                        processImport()
                                    } else {
                                        importStatus = ImportStatus.Error("No content pasted")
                                    }
                                },
                                enabled = pastedResponse.isNotEmpty()
                            ) {
                                Text("Import Response")
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
private fun parseJsonToTest(jsonString: String): MockTest { // Removed suspend modifier
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
    val questions = mutableListOf<Question>()
    
    for (i in 0 until questionsArray.length()) {
        val questionObj = questionsArray.getJSONObject(i)
        
        // Parse options array
        val optionsArray = questionObj.getJSONArray("options")
        val options = mutableListOf<String>()
        for (j in 0 until optionsArray.length()) {
            options.add(optionsArray.getString(j))
        }
        
        // Create question object
        val question = Question(
            text = questionObj.getString("text"),
            options = options,
            correctOptionIndex = questionObj.getInt("correctOptionIndex"),
            explanation = questionObj.getString("explanation"),
            difficulty = QuestionDifficulty.valueOf(
                questionObj.optString("difficulty", "MEDIUM")
            ),
            type = QuestionType.MULTIPLE_CHOICE,
            subject = questionObj.getString("subject"),
            topic = questionObj.getString("topic")
        )
        
        questions.add(question)
    }
    
    // Create and return the test
    return MockTest(
        name = name,
        difficulty = TestDifficulty.valueOf(difficultyString),
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
    object PromptMode : ImportStatus()
}

// Import mode to determine initial screen state
enum class ImportMode {
    FileImport,
    Prompt
}
