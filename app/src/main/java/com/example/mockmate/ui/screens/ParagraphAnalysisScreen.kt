package com.example.mockmate.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mockmate.MockMateApplication
import com.example.mockmate.model.ParagraphQuestion
import com.example.mockmate.ui.components.MockMateTopBar
import kotlinx.coroutines.launch

// Constants for UI state
private enum class AnalysisState {
    INPUT, LOADING, QUESTIONS, RESULTS, API_KEY_SETUP
}

@Composable
fun ParagraphAnalysisScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val apiConfig = MockMateApplication.getApiConfig()
    val aiQuestionGenerator = MockMateApplication.getAIQuestionGenerator()

    var paragraph by remember { mutableStateOf("") }
    var isGeneratingQuestions by remember { mutableStateOf(false) }
    var questions = remember { mutableStateListOf<ParagraphQuestion>() }
    var currentState by remember { mutableStateOf(AnalysisState.INPUT) }
    var newApiKeyProvider by remember { mutableStateOf("") }
    var newApiKeyValue by remember { mutableStateOf("") }
    var useAI by remember { mutableStateOf(false) }
    var numberOfQuestions by remember { mutableStateOf(5) }
    var networkError by remember { mutableStateOf<String?>(null) }

    var savedApiKeys by remember { mutableStateOf(apiConfig.getAllApiKeys()) }
    var selectedApiKeyProvider by remember { mutableStateOf<String?>(null) }
    var isApiKeyDropdownExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    // Removed unused snackbarHostState
    // val snackbarHostState = remember { SnackbarHostState() }

    // Set initial selected API key if any exist
    LaunchedEffect(savedApiKeys) {
        if (selectedApiKeyProvider == null && savedApiKeys.isNotEmpty()) {
            selectedApiKeyProvider = savedApiKeys.keys.first()
        }
    }

    LaunchedEffect(useAI, apiConfig.hasApiKeys) {
        if (useAI && !apiConfig.hasApiKeys) {
            currentState = AnalysisState.API_KEY_SETUP
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MockMateTopBar(
            title = "Paragraph Analysis",
            showBackButton = true,
            onBackClick = onNavigateBack
        )

        when (currentState) {
            AnalysisState.INPUT -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Analyze Your Knowledge",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Paste a paragraph you've read to generate questions and test your understanding",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = paragraph,
                        onValueChange = { paragraph = it },
                        label = { Text("Paste your paragraph here") },
                        placeholder = { Text("Enter or paste text that you want to test your knowledge on...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 8
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI option checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use AI for better questions",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Checkbox(
                            checked = useAI,
                            onCheckedChange = { newValue ->
                                useAI = newValue
                                if (useAI && !apiConfig.hasApiKeys) {
                                    currentState = AnalysisState.API_KEY_SETUP
                                }
                            }
                        )
                    }

                    // API Key Selection (only visible if useAI is checked and keys exist)
                    if (useAI && apiConfig.hasApiKeys) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select API Key:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box {
                                OutlinedTextField(
                                    value = selectedApiKeyProvider ?: "No keys saved",
                                    onValueChange = { }, // Read-only
                                    label = { Text("API Key Provider") },
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown arrow",
                                            Modifier.clickable { isApiKeyDropdownExpanded = true }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isApiKeyDropdownExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = isApiKeyDropdownExpanded,
                                    onDismissRequest = { isApiKeyDropdownExpanded = false }
                                ) {
                                    savedApiKeys.forEach { (provider, _) ->
                                        DropdownMenuItem(
                                            text = { Text(provider) },
                                            onClick = {
                                                selectedApiKeyProvider = provider
                                                isApiKeyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isGeneratingQuestions = true
                                currentState = AnalysisState.LOADING
                                networkError = null

                                try {
                                    questions.clear()

                                    val selectedKey = if (useAI && selectedApiKeyProvider != null) {
                                        apiConfig.getApiKey(selectedApiKeyProvider!!)
                                    } else {
                                        null
                                    }

                                    if (useAI && selectedKey.isNullOrEmpty()) {
                                         networkError = "Please select a valid API key or disable AI."
                                         currentState = AnalysisState.INPUT
                                     } else if (useAI && selectedKey != null) {
                                        Log.d("ParagraphAnalysisScreen", "Generating questions from paragraph: $paragraph")
                                        val generatedQuestions = try {
                                            aiQuestionGenerator.generateQuestionsFromParagraph(
                                                paragraph,
                                                numberOfQuestions,
                                                context.toString()
                                            )
                                        } catch (e: Exception) {
                                            Log.e("ParagraphAnalysisScreen", "Error generating questions: ${e.message}", e)
                                            networkError = "AI question generation failed: ${e.message}"
                                            null
                                        }

                                        if (generatedQuestions != null && generatedQuestions.isNotEmpty()) {
                                            questions.addAll(generatedQuestions)
                                            currentState = AnalysisState.QUESTIONS
                                        } else {
                                            // AI generation failed or not possible
                                            networkError = "AI question generation is currently unavailable. Please try again later."
                                            currentState = AnalysisState.INPUT // Stay on input screen
                                        }
                                    } else {
                                        // AI not used, or no key selected/available
                                        networkError = "AI is not enabled or no API key is selected/available."
                                        currentState = AnalysisState.INPUT // Stay on input screen
                                    }


                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    networkError = "An unexpected error occurred: ${e.message}"
                                    currentState = AnalysisState.INPUT // Stay on input screen
                                } finally {
                                    isGeneratingQuestions = false
                                }
                            }
                        },
                        enabled = paragraph.length >= 100 && !isGeneratingQuestions && (!useAI || (useAI && selectedApiKeyProvider != null && savedApiKeys[selectedApiKeyProvider]?.isNotEmpty() == true)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Questions & Start Test")
                    }

                    if (paragraph.length < 100) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please enter at least 100 characters for better question generation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (networkError != null) {
                        Spacer(modifier = Modifier.height(16.dp)) // Increased spacing
                        Text(
                            text = networkError ?: "",
                            style = MaterialTheme.typography.bodyMedium, // Increased text size
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center, // Center align error message
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            AnalysisState.LOADING -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Generating intelligent questions...",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This may take a moment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            AnalysisState.QUESTIONS -> {
                QuestionScreen(
                    questions = questions,
                    onSubmit = {
                        currentState = AnalysisState.RESULTS
                    },
                    onNavigateBack = {
                        currentState = AnalysisState.INPUT
                        questions.clear()
                    }
                )
            }
            AnalysisState.RESULTS -> {
                ResultsScreen(
                    questions = questions,
                    onDone = {
                        currentState = AnalysisState.INPUT
                        questions.clear()
                        paragraph = ""
                    },
                    onRetry = {
                        questions.forEach { question ->
                            question.selectedOptionIndex = null
                            question.isSubmitted = false
                        }
                        currentState = AnalysisState.QUESTIONS
                    }
                )
            }
            AnalysisState.API_KEY_SETUP -> {
                ApiKeySetupScreen(
                    providerName = newApiKeyProvider,
                    apiKey = newApiKeyValue,
                    onProviderNameChanged = { newApiKeyProvider = it },
                    onApiKeyChanged = { newApiKeyValue = it },
                    onSaveApiKey = {
                        if (newApiKeyProvider.isNotBlank() && newApiKeyValue.isNotBlank()) {
                            apiConfig.saveApiKey(newApiKeyProvider, newApiKeyValue)
                            savedApiKeys = apiConfig.getAllApiKeys() // Refresh saved keys
                            selectedApiKeyProvider = newApiKeyProvider // Select the newly saved key
                            newApiKeyProvider = "" // Clear input fields
                            newApiKeyValue = ""
                            currentState = AnalysisState.INPUT
                        } else {
                            // Show a toast or error message
                            Toast.makeText(context, "Provider name and API key cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCancel = {
                        currentState = AnalysisState.INPUT
                        newApiKeyProvider = ""
                        newApiKeyValue = ""
                    }
                )
            }
        }
    }
}

@Composable
fun QuestionScreen(
    questions: List<ParagraphQuestion>,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Use a state to force recomposition when an option is selected
    var refreshState by remember { mutableStateOf(0) }

    val allQuestionsAnswered = questions.all { it.selectedOptionIndex != null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Answer the Questions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Cancel")
        }

        Spacer(modifier = Modifier.height(16.dp))

        questions.forEachIndexed { index, question ->
            QuestionItem(
                questionIndex = index,
                question = question,
                onOptionSelected = { optionIndex ->
                    question.selectedOptionIndex = optionIndex
                    // Increment state to force recomposition of QuestionScreen
                    refreshState++
                }
            )

            if (index < questions.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            enabled = allQuestionsAnswered,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Answers")
        }

        if (!allQuestionsAnswered) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please answer all questions before submitting",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun QuestionItem(
    questionIndex: Int,
    question: ParagraphQuestion,
    onOptionSelected: (Int) -> Unit
) {
    var selectedOptionIndex by remember { mutableStateOf(question.selectedOptionIndex) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Question ${questionIndex + 1}:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = question.questionText,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        question.options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOptionIndex == index,
                    onClick = {
                        selectedOptionIndex = index
                        onOptionSelected(index)
                    }
                )

                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ResultsScreen(
    questions: List<ParagraphQuestion>,
    onDone: () -> Unit,
    onRetry: () -> Unit
) {
    val correctAnswers = questions.count { it.selectedOptionIndex == it.correctOptionIndex }
    val totalQuestions = questions.size
    val percentage = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
    val performanceText = when {
        percentage >= 90 -> "Excellent!"
        percentage >= 75 -> "Great job!"
        percentage >= 60 -> "Good effort!"
        else -> "Keep practicing!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Your Results",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = performanceText,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "You answered $correctAnswers out of $totalQuestions correctly ($percentage%)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Review Your Answers:",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        questions.forEachIndexed { index, question ->
            ResultQuestionItem(
                questionIndex = index,
                question = question
            )

            if (index < questions.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }
        }

        // Placeholder for Improvement Suggestions (requires more sophisticated logic/AI)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Improvement Suggestions:",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Based on your performance, consider reviewing the key concepts from the paragraph, especially areas related to questions you answered incorrectly. Practice analyzing complex sentences and identifying the main arguments.",
            style = MaterialTheme.typography.bodyMedium
        )


        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Done")
        }
    }
}

@Composable
fun ResultQuestionItem(
    questionIndex: Int,
    question: ParagraphQuestion
) {
    val isCorrect = question.selectedOptionIndex == question.correctOptionIndex

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${questionIndex + 1}:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isCorrect) "Correct" else "Incorrect",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = question.questionText,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        question.options.forEachIndexed { index, option ->
            val isSelected = question.selectedOptionIndex == index
            val isCorrectOption = question.correctOptionIndex == index

            val textColor = when {
                isCorrectOption -> Color(0xFF4CAF50) // Green for correct answer
                isSelected -> Color(0xFFF44336) // Red for incorrect selected answer
                else -> MaterialTheme.colorScheme.onSurface
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (index == question.correctOptionIndex) "✓" else if (isSelected) "✗" else "   ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCorrectOption -> Color(0xFF4CAF50)
                        isSelected -> Color(0xFFF44336)
                        else -> Color.Transparent // Hide icon if not selected and not correct
                    }
                )

                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (!isCorrect) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Answer: ${question.selectedOptionIndex?.let { question.options[it] } ?: "Not attempted"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF44336),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Correct Answer: ${question.options[question.correctOptionIndex]}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        } else {
             Spacer(modifier = Modifier.height(8.dp))
             Text(
                text = "Your Answer: ${question.selectedOptionIndex?.let { question.options[it] } ?: "Not attempted"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ApiKeySetupScreen(
    providerName: String,
    apiKey: String,
    onProviderNameChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Add New API Key",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter the provider name and API key for the AI service.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = providerName,
            onValueChange = onProviderNameChanged,
            label = { Text("Provider Name (e.g., Gemini)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChanged,
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onSaveApiKey,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Save API Key")
            }
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Cancel")
            }
        }
    }
}
