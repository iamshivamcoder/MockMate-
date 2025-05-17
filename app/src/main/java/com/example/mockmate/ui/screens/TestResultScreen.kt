package com.example.mockmate.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.UserAnswer
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.SectionHeader
import kotlinx.coroutines.flow.first

/**
 * Screen for displaying test results and analysis after completing a test
 */
@Composable
fun TestResultScreen(
    attemptId: String,
    testId: String,
    onNavigateBack: () -> Unit,
    onDashboardClick: () -> Unit,
    repository: TestRepository
) {
    // In a real app, we'd fetch the attempt from database
    // For now, we'll use mock data
    var test by remember { mutableStateOf<MockTest?>(null) }
    var attempt by remember { mutableStateOf<TestAttempt?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Debug log
    android.util.Log.d("TestResultScreen", "Received attemptId: $attemptId, testId: $testId")

    // Fetch test data
    LaunchedEffect(testId, attemptId) {
        try {
            isLoading = true
            loadError = null

            // Log for debugging
            android.util.Log.d("TestResultScreen", "Fetching test data for ID: $testId")

            // First get the test
            test = repository.getTestById(testId)
            android.util.Log.d("TestResultScreen", "Test loaded: ${test != null}")

            // Fetch the test attempt from the database
            attempt = repository.getTestAttemptById(attemptId)
            android.util.Log.d("TestResultScreen", "Attempt loaded: ${attempt != null}")

            isLoading = false
        } catch (e: Exception) {
            loadError = e.message
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MockMateTopBar(
            title = "Test Results",
            onBackClick = onNavigateBack
        )

        when {
            loadError != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Error loading results: $loadError",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
            isLoading || test == null || attempt == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading results...")
                }
            }
            else -> {
                ResultContent(
                    test = test!!,
                    attempt = attempt!!,
                    onDashboardClick = onDashboardClick
                )
            }
        }
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
        .mapValues { (subject, questions) ->
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
                icon = Icons.Default.ShowChart,
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
            val (total, attempted, correct) = performance
            val subjectAccuracy = if (attempted > 0) correct.toFloat() / attempted else 0f
            
            SubjectPerformanceCard(
                subject = subject,
                accuracy = subjectAccuracy,
                attempted = attempted,
                total = total,
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
                text = "${String.format("%.1f", score)}/${totalMarks.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${String.format("%.1f", scorePercentage)}%",
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
    total: Int,
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

private fun getScoreColor(percentage: Float): Color {
    return when {
        percentage >= 80 -> Color(0xFF4CAF50) // Green
        percentage >= 60 -> Color(0xFF2196F3) // Blue
        percentage >= 40 -> Color(0xFFFFC107) // Yellow
        else -> Color(0xFFF44336) // Red
    }
}

private fun getScoreFeedback(percentage: Float): String {
    return when {
        percentage >= 80 -> "Excellent! You've mastered this material."
        percentage >= 60 -> "Good job! You have a solid understanding of the material."
        percentage >= 40 -> "You're making progress, but should review some concepts."
        else -> "You need to focus on improving your understanding of this material."
    }
}
