package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.ui.components.MetricCard
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.OverallScoreCard
import com.shivams.mockmate.ui.components.SectionHeader
import com.shivams.mockmate.ui.components.SubjectPerformanceCard
import com.shivams.mockmate.ui.viewmodels.TestResultViewModel

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
    onAnalyticsClick: () -> Unit,
    onTestHistoryClick: () -> Unit,
    repository: TestRepository
) {
    val viewModel: TestResultViewModel = viewModel(
        factory = TestResultViewModelFactory(repository, testId, attemptId)
    )
    val uiState by viewModel.uiState.collectAsState()

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
                onBackClick = onNavigateBack
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
                        onDashboardClick = onDashboardClick,
                        onAnalyticsClick = onAnalyticsClick,
                        onTestHistoryClick = onTestHistoryClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultContent(
    test: MockTest,
    attempt: TestAttempt,
    onDashboardClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onTestHistoryClick: () -> Unit
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
    val scorePercentage = if (totalMarks > 0) totalScore / totalMarks * 100 else 0f

    // Group questions by subject
    val subjectPerformance = test.questions
        .groupBy { it.subject }
        .mapValues { (_, questions) ->
            val subjectAttempted = questions.count { question ->
                attempt.userAnswers[question.id]?.selectedOptionIndex != null
            }
            val subjectCorrect = questions.count { question ->
                val answer = attempt.userAnswers[question.id]
                answer?.selectedOptionIndex == question.correctOptionIndex
            }
            Pair(subjectAttempted, subjectCorrect)
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
            val (attempted, correct) = performance
            val subjectAccuracy = if (attempted > 0) correct.toFloat() / attempted else 0f

            SubjectPerformanceCard(
                subject = subject,
                accuracy = subjectAccuracy,
                attempted = attempted,
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
                    val (attempted, correct) = performance
                    attempted > 0 && correct.toFloat() / attempted < 0.7f
                }
                .sortedBy { (_, performance) ->
                    val (attempted, correct) = performance
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
            val isCorrect = isAnswered && userAnswer.selectedOptionIndex == question.correctOptionIndex

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
                            text = "Your Answer: ${question.options?.get(userAnswer.selectedOptionIndex ?: 0) ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = "Correct Answer: ${question.options?.get(question.correctOptionIndex ?: 0) ?: ""}",
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
                            text = "Correct Answer: ${question.options?.get(question.correctOptionIndex ?: 0) ?: ""}",
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onAnalyticsClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Analytics")
            }

            Button(
                onClick = onTestHistoryClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Test History")
            }
        }
    }
}
