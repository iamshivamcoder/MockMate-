package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.Question
import com.example.mockmate.model.QuestionStatus
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.OptionItem
import com.example.mockmate.ui.components.QuestionDifficultyBadge
import com.example.mockmate.ui.viewmodels.TestTakingScreenViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun TestTakingScreen(
    testId: String,
    onNavigateBack: () -> Unit,
    onFinish: (attemptId: String) -> Unit,
    repository: TestRepository
) {
    val viewModel: TestTakingScreenViewModel = viewModel(
        factory = TestTakingScreenViewModel.provideFactory(repository, testId)
    )

    val mockTest by viewModel.mockTest.collectAsState()
    var timeRemaining by remember { mutableLongStateOf(0L) }
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val questionStatus by viewModel.questionStatus.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    // Use local mutable state for question index and error dialog
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var showFinishDialog by remember { mutableStateOf(false) }
    val questionStartTimes = remember { mutableMapOf<Int, Long>() }
    var previousQuestionIndex by remember { mutableIntStateOf(0) }

    // Sync ViewModel time with local state
    val vmTimeRemaining by viewModel.timeRemaining.collectAsState()
    LaunchedEffect(vmTimeRemaining) {
        timeRemaining = vmTimeRemaining
    }

    if (mockTest == null) {
        // Test not found or loading
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            Text("Test not found")
            Button(onClick = onNavigateBack) {
                Text("Go Back")
            }
        }
        return
    }

    // Track time when switching questions
    LaunchedEffect(currentQuestionIndex) {
        if (currentQuestionIndex != previousQuestionIndex) {
            questionStartTimes[previousQuestionIndex]?.let { startTime ->
                val timeSpent = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                viewModel.updateQuestionTimeSpent(previousQuestionIndex, timeSpent)
            }
            questionStartTimes[currentQuestionIndex] = System.currentTimeMillis()
            previousQuestionIndex = currentQuestionIndex
        }
    }

    // Timer effect
    LaunchedEffect(key1 = mockTest) {
        // mockTest is guaranteed to be non-null here due to the check above
        while (timeRemaining > 0) {
            delay(1.seconds)
            timeRemaining--
            viewModel.updateTimeRemaining(timeRemaining)
            if (timeRemaining <= 0) {
                showFinishDialog = true
            }
        }
    }

    val currentQuestion = mockTest!!.questions[currentQuestionIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        MockMateTopBar(
            title = mockTest!!.name,
            onBackClick = { showFinishDialog = true }
        )

        // Timer and progress
        TestProgressHeader(
            currentQuestion = currentQuestionIndex + 1,
            totalQuestions = mockTest!!.questions.size,
            timeRemaining = timeRemaining
        )

        // Question content
        QuestionContent(
            modifier = Modifier.weight(1f),
            question = currentQuestion,
            selectedOptionIndex = selectedOptions[currentQuestionIndex],
            onOptionSelected = { optionIndex ->
                viewModel.updateSelectedOption(currentQuestionIndex, optionIndex)
                if (optionIndex != -1) {
                    viewModel.updateQuestionStatus(currentQuestionIndex, QuestionStatus.ANSWERED)
                }
            }
        )

        // Navigation buttons
        TestNavigationFooter(
            currentQuestionIndex = currentQuestionIndex,
            totalQuestions = mockTest!!.questions.size,
            onPreviousClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
            onNextClick = { if (currentQuestionIndex < mockTest!!.questions.size - 1) currentQuestionIndex++ },
            onFinishClick = { showFinishDialog = true },
            onBookmarkClick = {
                viewModel.toggleBookmark(currentQuestionIndex)
            },
            onMarkForReviewClick = {
                viewModel.toggleMarkForReview(currentQuestionIndex)
            },
            isBookmarked = questionStatus[currentQuestionIndex] == QuestionStatus.BOOKMARKED,
            isMarkedForReview = questionStatus[currentQuestionIndex] == QuestionStatus.MARKED_FOR_REVIEW
        )
    }

    // Handle test completion
    if (showFinishDialog) {
        // Update time spent on current question before finishing
        questionStartTimes[currentQuestionIndex]?.let { startTime ->
            val timeSpent = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            viewModel.updateQuestionTimeSpent(currentQuestionIndex, timeSpent)
        }

        val attemptedCount = selectedOptions.count { it != -1 }

        if (!isSaving) {
            FinishTestDialog(
                onDismiss = { showFinishDialog = false },
                onConfirm = {
                    viewModel.submitTestAttempt { attemptId ->
                        onFinish(attemptId)
                    }
                },
                attemptedQuestions = attemptedCount,
                totalQuestions = mockTest!!.questions.size
            )
        }
    }

    // Show error dialog if there's an error
    val errorMsg by viewModel.errorMessage.collectAsState()
    errorMsg?.let { message ->
        // Hide loading dialog if error occurs
        // isSaving is a val, so don't try to reassign it
        android.util.Log.e("TestTakingScreen", "Error dialog shown: $message")
        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
                showFinishDialog = false // Also dismiss finish dialog
            },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearError()
                    showFinishDialog = false // Reset finish dialog state
                }) {
                    Text("OK")
                }
            }
        )
    }

    // MIUI/Android battery optimization warning
    val showBatteryDialog = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Removed empty if block that previously checked for xiaomi/miui
    }
    if (showBatteryDialog.value) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog.value = false },
            title = { Text("Battery Optimization Warning") },
            text = { Text("To ensure smooth operation, please disable battery optimization for this app in your device settings. Otherwise, the app may be killed in the background and you may experience stuck loading screens or lost progress.") },
            confirmButton = {
                Button(onClick = { showBatteryDialog.value = false }) { Text("OK") }
            }
        )
    }

    // Debug: Log when composable is recomposed
    android.util.Log.d("TestTakingScreen", "Recomposed. isSaving=$isSaving, errorMessage=$errorMsg")
}

@Composable
fun TestProgressHeader(
    currentQuestion: Int,
    totalQuestions: Int,
    timeRemaining: Long
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question $currentQuestion of $totalQuestions",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formatTime(timeRemaining),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (timeRemaining < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = { currentQuestion.toFloat() / totalQuestions },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun QuestionContent(
    modifier: Modifier = Modifier,
    question: Question,
    selectedOptionIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Question difficulty badge
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionDifficultyBadge(difficulty = question.difficulty)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${question.subject} > ${question.topic}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Question text
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options
        Text(
            text = "Select an answer:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        question.options.forEachIndexed { index, option ->
            OptionItem(
                optionText = option,
                selected = selectedOptionIndex == index,
                onOptionClick = { onOptionSelected(index) }
            )
        }
    }
}

@Composable
fun TestNavigationFooter(
    currentQuestionIndex: Int,
    totalQuestions: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onFinishClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onMarkForReviewClick: () -> Unit,
    isBookmarked: Boolean,
    isMarkedForReview: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Action buttons
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onMarkForReviewClick) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Mark for review",
                    tint = if (isMarkedForReview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentQuestionIndex > 0) {
                    Button(
                        onClick = onPreviousClick,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
            }

            if (currentQuestionIndex < totalQuestions - 1) {
                    Button(
                        onClick = onNextClick,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next"
                        )
                    }
            } else {
                Button(
                    onClick = onFinishClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("Finish Test")
                }
            }
        }
    }
}

@Composable
fun FinishTestDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    attemptedQuestions: Int,
    totalQuestions: Int
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish Test?") },
        text = {
            Column {
                Text("You have answered $attemptedQuestions out of $totalQuestions questions.")

                if (attemptedQuestions < totalQuestions) {
                    Text(
                        text = "There are ${totalQuestions - attemptedQuestions} unanswered questions.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    text = "Are you sure you want to finish this test?",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Yes, Finish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Test")
            }
        }
    )
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
}

// Removed unused function createTestAttempt

// Removed unused function calculateTimeSpentOnQuestion
