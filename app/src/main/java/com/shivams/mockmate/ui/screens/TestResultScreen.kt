package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.SectionHeader
import com.shivams.mockmate.ui.viewmodels.TestResultViewModel
import java.util.Locale

private const val EXCELLENT_THRESHOLD = 80
private const val GOOD_THRESHOLD = 60
private const val PROGRESS_THRESHOLD = 40

private val COLOR_EXCELLENT = Color(0xFF4CAF50) // Green
private val COLOR_GOOD = Color(0xFF2196F3) // Blue
private val COLOR_PROGRESS = Color(0xFFFFC107) // Yellow
private val COLOR_NEEDS_IMPROVEMENT = Color(0xFFF44336) // Red

private const val FEEDBACK_EXCELLENT = "Excellent! You\'ve mastered this material."
private const val FEEDBACK_GOOD = "Good job! You have a solid understanding of the material."
private const val FEEDBACK_PROGRESS = "You\'re making progress, but should review some concepts."
private const val FEEDBACK_NEEDS_IMPROVEMENT = "You need to focus on improving your understanding of this material."


// ViewModelFactory for TestResultViewModel
@Suppress("UNCHECKED_CAST")
class TestResultViewModelFactory(
    private val repository: TestRepository,
    private val testId: String,
    private val attemptId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestResultViewModel::class.java)) {
            return TestResultViewModel(repository, testId, attemptId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


/**
 * Screen for displaying test results and analysis after completing a test
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    attemptId: String,
    testId: String,
    onNavigateBack: () -> Unit,
    onDashboardClick: () -> Unit,
    repository: TestRepository
) {
    val viewModel: TestResultViewModel = viewModel(
        factory = TestResultViewModelFactory(repository, testId, attemptId)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    // Navigate back when deletion is confirmed
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            MockMateTopBar(
                title = uiState.testAttempt?.customName ?: uiState.mockTest?.name ?: "Test Result",
                onBackClick = onNavigateBack,
                dropdownContent = {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            newName = uiState.testAttempt?.customName ?: uiState.mockTest?.name ?: ""
                            showRenameDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { showDeleteDialog = true }
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.mockTest != null && uiState.testAttempt != null -> {
                    ResultContent(
                        test = uiState.mockTest!!,
                        attempt = uiState.testAttempt!!,
                        onDashboardClick = onDashboardClick
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Attempt") },
            text = { Text("Are you sure you want to permanently delete this test attempt?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteTestAttempt()
                    showDeleteDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Test") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New test name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.renameTestAttempt(newName)
                    showRenameDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ResultContent(
    test: MockTest,
    attempt: TestAttempt,
    onDashboardClick: () -> Unit
) {
    // Calculate stats
    val totalQuestions = test.questions.size
    val attemptedQuestions = attempt.userAnswers.count { it.value.selectedOptionIndex != null }
    val correctAnswers = attempt.userAnswers.count { (questionId, answer) ->
        val question = test.questions.find { it.id == questionId }
        question?.correctOptionIndex == answer.selectedOptionIndex
    }

    val accuracy = if (attemptedQuestions > 0) {
        correctAnswers.toFloat() / attemptedQuestions
    } else {
        0f
    }

    val totalScore = correctAnswers -
            if (test.negativeMarking) {
                (attemptedQuestions - correctAnswers) * test.negativeMarkingValue
            } else {
                0f
            }

    val totalMarks = test.questions.size.toFloat()
    val scorePercentage = totalScore / totalMarks * 100

    // Group questions by subject
    val subjectPerformance = test.questions
        .groupBy { it.subject }
        .mapValues { (_, questions) -> // Removed unused 'subject' parameter
            val subjectQuestions = questions.size
            val subjectAttempted = questions.count { question ->
                attempt.userAnswers[question.id]?.selectedOptionIndex != null
            }
            val subjectCorrect = questions.count { question ->
                val answer = attempt.userAnswers[question.id]
                answer?.selectedOptionIndex == question.correctOptionIndex
            }
            Triple(subjectQuestions, subjectAttempted, subjectCorrect)
        }

    // Calculate average time per question
    val totalTime = attempt.userAnswers.values.sumOf { it.timeSpent }
    val avgTimePerQuestion = if (attemptedQuestions > 0) {
        totalTime / attemptedQuestions
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Overall score card
        OverallScoreCard(
            score = totalScore,
            totalMarks = totalMarks,
            scorePercentage = scorePercentage
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Performance breakdown
        SectionHeader(text = "Performance Breakdown")

        // Key metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricCard(
                title = "Accuracy",
                value = "${(accuracy * 100).toInt()}%",
                icon = Icons.Default.Check,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            MetricCard(
                title = "Attempted",
                value = "$attemptedQuestions/$totalQuestions",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            MetricCard(
                title = "Avg Time",
                value = "${avgTimePerQuestion}s",
                icon = Icons.Default.Timer,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subject-wise performance
        SectionHeader(text = "Subject-wise Performance")

        subjectPerformance.forEach { (subject, performance) ->
            val (_, attempted, correct) = performance // Renamed 'total' to 'totalInSubject'
            val subjectAccuracy = if (attempted > 0) correct.toFloat() / attempted else 0f

            SubjectPerformanceCard(
                subject = subject,
                accuracy = subjectAccuracy,
                attempted = attempted,
                // totalInSubject = totalInSubject, // Pass renamed 'totalInSubject'
                correct = correct
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Areas to focus on
        if (subjectPerformance.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "Areas to Focus On")

            // Find subjects with lowest accuracy
            val weakestSubjects = subjectPerformance.entries
                .filter { (_, performance) ->
                    val (_, attempted, correct) = performance
                    attempted > 0 && correct.toFloat() / attempted < 0.7f
                }
                .sortedBy { (_, performance) ->
                    val (_, attempted, correct) = performance
                    if (attempted > 0) correct.toFloat() / attempted else 0f
                }
                .take(2)
                .map { it.key }

            if (weakestSubjects.isNotEmpty()) {
                Text(
                    text = "Focus on improving your knowledge in:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )

                weakestSubjects.forEach { subject ->
                    Text(
                        text = "• $subject",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 24.dp, top = 4.dp)
                    )
                }
            } else {
                Text(
                    text = "Great job! Your performance is strong across all subjects.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Question-wise breakdown
        SectionHeader(text = "Question-wise Breakdown")

        test.questions.forEachIndexed { index, question ->
            val userAnswer = attempt.userAnswers[question.id]
            val isAnswered = userAnswer?.selectedOptionIndex != null
            val isCorrect = isAnswered && userAnswer?.selectedOptionIndex == question.correctOptionIndex

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Question ${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isAnswered) {
                        Text(
                            text = "Your Answer: ${question.options?.get(userAnswer?.selectedOptionIndex ?: 0)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = "Correct Answer: ${question.options?.get(question.correctOptionIndex ?: 0)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (!isCorrect) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Explanation: ${question.explanation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Not Answered",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = "Correct Answer: ${question.options?.get(question.correctOptionIndex ?: 0)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Explanation: ${question.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDashboardClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Dashboard")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OverallScoreCard(
    score: Float,
    totalMarks: Float,
    scorePercentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Score",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${String.format(Locale.getDefault(), "%.1f", score)}/${totalMarks.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${String.format(Locale.getDefault(), "%.1f", scorePercentage)}%",
                style = MaterialTheme.typography.titleLarge,
                color = getScoreColor(scorePercentage)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                progress = { scorePercentage / 100f },
                color = getScoreColor(scorePercentage),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getScoreFeedback(scorePercentage),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SubjectPerformanceCard(
    subject: String,
    accuracy: Float,
    attempted: Int,
    // totalInSubject: Int, // Renamed 'total' to 'totalInSubject' - NOW REMOVED
    correct: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Accuracy: ${(accuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        progress = { accuracy },
                        color = getScoreColor(accuracy * 100),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$correct/$attempted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "questions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Returns a color based on the score percentage.
 */
private fun getScoreColor(percentage: Float): Color {
    return when {
        percentage >= EXCELLENT_THRESHOLD -> COLOR_EXCELLENT
        percentage >= GOOD_THRESHOLD -> COLOR_GOOD
        percentage >= PROGRESS_THRESHOLD -> COLOR_PROGRESS
        else -> COLOR_NEEDS_IMPROVEMENT
    }
}

/**
 * Returns feedback text based on the score percentage.
 */
private fun getScoreFeedback(percentage: Float): String {
    return when {
        percentage >= EXCELLENT_THRESHOLD -> FEEDBACK_EXCELLENT
        percentage >= GOOD_THRESHOLD -> FEEDBACK_GOOD
        percentage >= PROGRESS_THRESHOLD -> FEEDBACK_PROGRESS
        else -> FEEDBACK_NEEDS_IMPROVEMENT
    }
}
