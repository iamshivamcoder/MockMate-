package com.example.mockmate.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mockmate.ApiConfig
import com.example.mockmate.MockMateApplication
import com.example.mockmate.model.AIQuestionDifficulty
import com.example.mockmate.model.ParagraphQuestion
import com.example.mockmate.model.subjects
import com.example.mockmate.service.AIQuestionGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIQuestionScreen(navController: NavController = rememberNavController(), apiKey: String) {
    var selectedSubject by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(AIQuestionDifficulty.MEDIUM) }
    var numberOfQuestions by remember { mutableStateOf(5) }
    var questions by remember { mutableStateOf<List<ParagraphQuestion>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val geminiApiService = remember { MockMateApplication.getGeminiApiService() }
    val aiQuestionGenerator = remember { AIQuestionGenerator(geminiApiService) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, Int?>()) }

    fun calculateAccuracy(): Float {
        if (questions.isEmpty()) return 0f
        val correctAnswers = questions.mapIndexed { index, question ->
            val selectedOption = selectedAnswers[index]
            if (selectedOption != null && selectedOption == question.correctOptionIndex) 1 else 0
        }.sum()
        return correctAnswers.toFloat() / questions.size
    }

    fun getSubjects(): List<String> {
        return if (selectedSubject.isNotEmpty()) listOf(selectedSubject) else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "AI Generated Questions", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Customize your practice session.")

        // Subject Selection
        Text(text = "Select Subject:")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            subjects.forEach { subject ->
                FilterChip(
                    selected = selectedSubject == subject,
                    onClick = { selectedSubject = subject },
                    label = { Text(text = subject) }
                )
            }
        }

        // Difficulty Selection
        Text(text = "Select Difficulty: ${difficulty.name}")
        Slider(
            value = when (difficulty) {
                AIQuestionDifficulty.EASY -> 0f
                AIQuestionDifficulty.MEDIUM -> 0.5f
                AIQuestionDifficulty.HARD -> 1f
            },
            onValueChange = {
                difficulty = when {
                    it < 0.33 -> AIQuestionDifficulty.EASY
                    it < 0.66 -> AIQuestionDifficulty.MEDIUM
                    else -> AIQuestionDifficulty.HARD
                }
            },
            valueRange = 0f..1f,
            steps = 2
        )

        // Number of Questions Selection
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Number of Questions: $numberOfQuestions")
            Switch(
                checked = numberOfQuestions > 5,
                onCheckedChange = { numberOfQuestions = if (it) 10 else 5 }
            )
        }

        Button(onClick = {
            scope.launch {
                val generatedQuestions = aiQuestionGenerator.generateQuestionsFromParagraph(
                    paragraph = "This is a sample paragraph for generating AI questions.",
                    numQuestions = numberOfQuestions,
                    apiKey = apiKey,
                    context = context
                )
                questions = generatedQuestions
                selectedAnswers = mutableMapOf()
            }
        }) {
            Text(text = "Generate Questions")
        }

        // Display Questions
        if (questions.isNotEmpty()) {
            Text(text = "Generated Questions:")
            questions.forEachIndexed { index, question ->
                QuestionCard(
                    index = index + 1,
                    question = question,
                    onOptionSelected = { optionIndex ->
                        selectedAnswers[index] = optionIndex
                    },
                    selectedOption = selectedAnswers[index]
                )
            }

            Button(onClick = {
                if (questions.isNotEmpty() && selectedAnswers.size == questions.size) {
                    val accuracy = calculateAccuracy()
                    val subjects = getSubjects()
                    navController.navigate("ai_question_result/${accuracy}/${subjects.joinToString(",")}")
                } else {
                    // Optionally, show a message to the user that they need to answer all questions
                    // For example, using a Toast or a Snackbar
                    // Toast.makeText(context, "Please answer all questions", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Submit Answers")
            }
        }
    }
}

@Composable
fun QuestionCard(
    index: Int,
    question: ParagraphQuestion,
    onOptionSelected: (Int) -> Unit,
    selectedOption: Int?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "$index. ${question.questionText}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                question.options.forEachIndexed { optionIndex, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == optionIndex,
                            onClick = { onOptionSelected(optionIndex) }
                        )
                        Text(text = option)
                    }
                }
            }

            Text(text = "Powered by AI", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AIQuestionScreenPreview() {
    val navController = rememberNavController()
    AIQuestionScreen(navController = navController, apiKey = "")
}