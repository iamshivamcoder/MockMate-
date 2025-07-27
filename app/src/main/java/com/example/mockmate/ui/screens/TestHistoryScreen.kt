package com.example.mockmate.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.TestRepository
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TestHistoryScreen(
    onNavigateBack: () -> Unit,
    onViewTestResult: (String, String) -> Unit,
    repository: TestRepository
) {
    val testAttemptsFlow = repository.getAllTestAttempts()
    val testAttemptsList by testAttemptsFlow.collectAsState(initial = emptyList())

    var isLoading by remember { mutableStateOf(true) }
    var attemptsWithTest by remember { mutableStateOf<List<AttemptWithTest>>(emptyList()) }

    LaunchedEffect(testAttemptsList) {
        isLoading = true
        val processedAttempts = testAttemptsList.mapNotNull { attempt ->
            val test = repository.getTestById(attempt.testId)
            if (test != null) {
                val attemptedQuestions = attempt.userAnswers.size
                var correctAnswers = 0
                attempt.userAnswers.forEach { (questionId, userAnswer) ->
                    val question = test.questions.find { it.id == questionId }
                    if (question != null && userAnswer.selectedOptionIndex == question.correctOptionIndex) {
                        correctAnswers++
                    }
                }
                AttemptWithTest(
                    attemptId = attempt.id,
                    testId = attempt.testId,
                    testName = test.name,
                    date = attempt.startTime,
                    attemptedQuestions = attemptedQuestions,
                    correctAnswers = correctAnswers,
                    totalQuestions = test.questions.size,
                    score = correctAnswers // Assuming score is correct answers for now
                )
            } else {
                null // Skip attempts for tests that are not found
            }
        }
        attemptsWithTest = processedAttempts
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MockMateTopBar(
            title = "Test History & Analytics",
            onBackClick = onNavigateBack
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            TestHistoryContent(
                testAttempts = attemptsWithTest,
                onViewResult = { attemptId, testId ->
                    onViewTestResult(attemptId, testId)
                }
            )
        }
    }
}

@Composable
private fun TestHistoryContent(
    testAttempts: List<AttemptWithTest>,
    onViewResult: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Summary statistics
        SectionHeader(text = "Your Progress")

        val totalAttemptedQuestions = testAttempts.sumOf { it.attemptedQuestions }
        val totalCorrectAnswers = testAttempts.sumOf { it.correctAnswers }

        val accuracy = if (totalAttemptedQuestions > 0) {
            totalCorrectAnswers.toFloat() / totalAttemptedQuestions.toFloat()
        } else 0f

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProgressStat(
                title = "Tests Taken",
                value = testAttempts.size.toString(),
                modifier = Modifier.weight(1f)
            )

            ProgressStat(
                title = "Avg. Score",
                value = "${(accuracy * 100).toInt()}%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test history list
        SectionHeader(text = "Test History")
        
        // Sorting options
        var sortBy by remember { mutableStateOf("Date") }
        var sortAscending by remember { mutableStateOf(false) }
        var showSortMenu by remember { mutableStateOf(false) }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sort by: $sortBy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (sortAscending) Icons.Default.DateRange else Icons.Default.Assessment, // Use icons to indicate order
                    contentDescription = if (sortAscending) "Ascending" else "Descending",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (sortAscending) " (Asc)" else " (Desc)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Sort options",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.widthIn(min = 180.dp) // Make menu more responsive
                ) {
                    DropdownMenuItem(
                        text = { Text("Date") },
                        onClick = {
                            sortBy = "Date"
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Score") },
                        onClick = {
                            sortBy = "Score"
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Test Name") },
                        onClick = {
                            sortBy = "Test Name"
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (sortAscending) "Ascending" else "Descending") },
                        onClick = {
                            sortAscending = !sortAscending
                            showSortMenu = false
                        }
                    )
                }
            }
        }
        
        // Sort the test attempts based on selected criteria
        val sortedAttempts = when (sortBy) {
            "Date" -> if (sortAscending) testAttempts.sortedBy { it.date } else testAttempts.sortedByDescending { it.date }
            "Score" -> if (sortAscending) testAttempts.sortedBy { it.score } else testAttempts.sortedByDescending { it.score }
            "Test Name" -> if (sortAscending) testAttempts.sortedBy { it.testName } else testAttempts.sortedByDescending { it.testName }
            else -> testAttempts
        }

        if (sortedAttempts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You haven't taken any tests yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn {
                items(sortedAttempts) { attempt ->
                    TestHistoryItem(
                        attempt = attempt,
                        onViewResult = { onViewResult(attempt.attemptId, attempt.testId) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // Add some bottom spacing
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProgressStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TestHistoryItem(
    attempt: AttemptWithTest,
    onViewResult: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewResult() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row with test name and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = attempt.testName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Result") },
                            leadingIcon = {
                                Icon(Icons.Default.Assessment, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onViewResult()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date and score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 4.dp)
                )

                Text(
                    text = formatDate(attempt.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${attempt.score}/${attempt.totalQuestions}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "(${(attempt.score.toFloat() / attempt.totalQuestions * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress indicator
            val progress = if (attempt.totalQuestions > 0) attempt.score.toFloat() / attempt.totalQuestions else 0f
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(6.dp),
                progress = { progress }
            )
        }
    }
}

// Helper class to combine test and attempt info
data class AttemptWithTest(
    val attemptId: String,
    val testId: String,
    val testName: String,
    val date: Date,
    val attemptedQuestions: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val score: Int
)

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(date)
}
