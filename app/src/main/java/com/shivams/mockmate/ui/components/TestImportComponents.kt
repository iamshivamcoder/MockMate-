package com.shivams.mockmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.ui.screens.ImportStatus

@Composable
fun ImportFromFileCard(
    selectedFileName: String,
    onSelectFileClick: () -> Unit,
    onImportClick: () -> Unit,
    onImportFromPromptClick: () -> Unit,
    importStatus: ImportStatus,
    fileContent: String?
) {
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

            FileSelectionArea(selectedFileName, onSelectFileClick)

            Spacer(modifier = Modifier.height(16.dp))

            if (importStatus is ImportStatus.Ready) {
                Button(
                    onClick = onImportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    enabled = fileContent != null
                ) {
                    Text("Import Test Data")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onImportFromPromptClick,
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
}

@Composable
fun FileSelectionArea(selectedFileName: String, onSelectFileClick: () -> Unit) {
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

            OutlinedButton(onClick = onSelectFileClick) {
                Text("Select JSON File")
            }
        }
    }
}

@Composable
fun ImportStatusView(
    importStatus: ImportStatus,
    onImportAnother: () -> Unit,
    onViewTests: () -> Unit,
    onTryAgain: () -> Unit
) {
    when (importStatus) {
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
            SuccessCard(importStatus.testName, importStatus.questionCount, onImportAnother, onViewTests)
        }
        is ImportStatus.Error -> {
            ErrorCard(importStatus.message, onTryAgain)
        }
        else -> { /* Do nothing for Ready or PromptMode */ }
    }
}

@Composable
fun SuccessCard(
    testName: String,
    questionCount: Int,
    onImportAnother: () -> Unit,
    onViewTests: () -> Unit
) {
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
                text = "Name: $testName",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Questions: $questionCount",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = onImportAnother) {
                    Text("Import Another")
                }

                Button(onClick = onViewTests) {
                    Text("View Tests")
                }
            }
        }
    }
}

@Composable
fun ErrorCard(message: String, onTryAgain: () -> Unit) {
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
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = onTryAgain) {
                Text("Try Again")
            }
        }
    }
}

@Composable
fun ImportFromPromptCard(
    onCancel: () -> Unit,
    onImport: (String) -> Unit
) {
    val context = LocalContext.current
    var pastedResponse by remember { mutableStateOf("") }

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
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Prompt", "I am preparing a mock test generator for UPSC Civil Services Examination (CSE) preparation. Before generating the test in JSON format, I need to gather specific requirements. Please ask the following questions to customize the mock test:\n1. Which specific subject areas or topics do you want to focus on (e.g., History, Geography, Polity, Economy, Environment, Science & Technology, Current Affairs)?\n2. How many questions would you like in the test?\n3. What should be the difficulty level of the questions (EASY, MEDIUM, HARD, or a mix)?\nAfter receiving my answers to these questions, confirm the details with me. Only after my confirmation, generate the mock test in JSON format. The JSON should include test name, difficulty (EASY, MEDIUM, HARD), time limit in minutes, negative marking (true/false), negative marking value (if applicable), and an array of questions. Each question must have text, an array of 4 options, correct option index (0-3), detailed explanation, subject, topic within the subject, and difficulty (EASY, MEDIUM, HARD). Ensure questions are of UPSC CSE standard, covering analytical and conceptual understanding. Format the JSON exactly as shown in the example below to ensure compatibility with the app:\n\n{\n  \"name\": \"UPSC CSE Prelims Mock Test 1\",\n  \"difficulty\": \"HARD\",\n  \"timeLimit\": 120,\n  \"negativeMarking\": true,\n  \"negativeMarkingValue\": 0.66,\n  \"questions\": [\n    {\n      \"text\": \"Which of the following is a fundamental duty under the Indian Constitution?\",\n      \"options\": [\"To vote in elections\", \"To pay taxes\", \"To safeguard public property\", \"To follow traffic rules\"],\n      \"correctOptionIndex\": 2,\n      \"explanation\": \"Article 51A of the Indian Constitution lists fundamental duties, including the duty to safeguard public property and to abjure violence.\",\n      \"subject\": \"Indian Polity\",\n      \"topic\": \"Constitution\",\n      \"difficulty\": \"MEDIUM\"\n    }\n    // Add more questions here\n  ]\n}")
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(context, "Prompt copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
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
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
            Button(
                onClick = { onImport(pastedResponse) },
                enabled = pastedResponse.isNotEmpty()
            ) {
                Text("Import Response")
            }
        }
    }
}

@Composable
fun JsonFormatExample() {
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
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ImportHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to Import Tests") },
        text = {
            Text(
                "You can import tests from a JSON file or by pasting the JSON content directly.\n\n" +
                        "1. From File: Tap \"Select JSON File\" and choose a compatible file.\n" +
                        "2. From Prompt: Tap \"Import from Prompt\", copy the generated prompt, and paste it into an LLM. Then, paste the LLM\'s JSON output back into the app.\n\n" +
                        "Ensure the JSON follows the format specified in the example on this screen."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
