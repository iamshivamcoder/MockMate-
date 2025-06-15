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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.InMemoryTestRepository
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.Question
import com.example.mockmate.model.QuestionStatus
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.UserAnswer
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.OptionItem
import com.example.mockmate.ui.components.QuestionDifficultyBadge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.snapshotFlow
import java.util.Date
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Composable
fun TestTakingScreen(
    testId: String,
    onNavigateBack: () -> Unit,
    onFinish: (attemptId: String) -> Unit,
    repository: TestRepository
) {
    var mockTest by remember { mutableStateOf<MockTest?>(null) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var timeRemaining by remember { mutableLongStateOf(0L) }
    var selectedOptions by remember { mutableStateOf<List<Int>>(emptyList()) }
    var questionStatus by remember { mutableStateOf<List<QuestionStatus>>(emptyList()) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Track time spent on each question
    var questionTimeSpent by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    var questionStartTimes by remember { mutableStateOf(mutableMapOf<Int, Long>()) }
    var previousQuestionIndex by remember { mutableStateOf(0) }

    // Fetch the test
    LaunchedEffect(testId) {
        try {
            mockTest = repository.getTestById(testId)
            mockTest?.let { test ->
                timeRemaining = test.timeLimit.toLong() * 60
                selectedOptions = List(test.questions.size) { -1 }
                questionStatus = List(test.questions.size) { QuestionStatus.UNATTEMPTED }
                questionStartTimes[0] = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            android.util.Log.e("TestTakingScreen", "Error loading test: ${e.message}", e)
            errorMessage = "Failed to load test: ${e.message}"
        }
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
                questionTimeSpent[previousQuestionIndex] = (questionTimeSpent[previousQuestionIndex] ?: 0) + timeSpent
            }
            questionStartTimes[currentQuestionIndex] = System.currentTimeMillis()
            previousQuestionIndex = currentQuestionIndex
        }
    }

    // Timer effect
    LaunchedEffect(key1 = mockTest) {
        if (mockTest != null) {
            val startTime = System.currentTimeMillis()
            while (timeRemaining > 0) {
                delay(1.seconds)
                timeRemaining--
            }

            // Time's up, auto-submit
            showFinishDialog = true
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
                selectedOptions = selectedOptions.toMutableList().also {
                    it[currentQuestionIndex] = optionIndex
                }

                // Update question status
                if (optionIndex != -1 && questionStatus[currentQuestionIndex] == QuestionStatus.UNATTEMPTED) {
                    questionStatus = questionStatus.toMutableList().also {
                        it[currentQuestionIndex] = QuestionStatus.ANSWERED
                    }
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
                questionStatus = questionStatus.toMutableList().also {
                    it[currentQuestionIndex] = when (it[currentQuestionIndex]) {
                        QuestionStatus.BOOKMARKED -> QuestionStatus.UNATTEMPTED
                        else -> QuestionStatus.BOOKMARKED
                    }
                }
            },
            onMarkForReviewClick = {
                questionStatus = questionStatus.toMutableList().also {
                    it[currentQuestionIndex] = when (it[currentQuestionIndex]) {
                        QuestionStatus.MARKED_FOR_REVIEW -> QuestionStatus.UNATTEMPTED
                        else -> QuestionStatus.MARKED_FOR_REVIEW
                    }
                }
            },
            isBookmarked = questionStatus[currentQuestionIndex] == QuestionStatus.BOOKMARKED,
            isMarkedForReview = questionStatus[currentQuestionIndex] == QuestionStatus.MARKED_FOR_REVIEW
        )
    }

    // Handle test completion
    if (showFinishDialog) {
        // Update time spent on current question before finishing
        val currentQuestionTimeSpent = calculateTimeSpentOnQuestion(
            currentQuestionIndex,
            System.currentTimeMillis()
        )

        questionTimeSpent = questionTimeSpent.toMutableMap().apply {
            this[currentQuestionIndex] = currentQuestionTimeSpent
        }

        val attemptedCount = selectedOptions.count { it != -1 }

        if (!isSaving) {
            FinishTestDialog(
                onDismiss = {
                    showFinishDialog = false
                },
                onConfirm = {
                    val testAttempt = createTestAttempt(
                        testId = testId,
                        selectedOptions = selectedOptions,
                        questionStatus = questionStatus,
                        questionTimeSpent = questionTimeSpent,
                        timeRemaining = timeRemaining,
                        mockTest = mockTest!!
                    )

                    // Launch save operation
                    isSaving = true

                    // Save attempt in a coroutine
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.saveTestAttempt(testAttempt)
                            withContext(Dispatchers.Main) {
                                isSaving = false
                                onFinish(testAttempt.id)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isSaving = false
                                errorMessage = e.message
                            }
                        }
                    }
                },
                attemptedQuestions = attemptedCount,
                totalQuestions = mockTest!!.questions.size
            )
        }
    }

    // Show loading dialog while saving
    if (isSaving) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Saving test attempt...") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Please wait while we save your test attempt...")
                }
            },
            confirmButton = { }
        )
    }

    // Show error dialog if there's an error
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
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
                        imageVector = Icons.Default.ArrowBack,
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
                        imageVector = Icons.Default.ArrowForward,
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

fun createTestAttempt(
    testId: String,
    selectedOptions: List<Int>,
    questionStatus: List<QuestionStatus>,
    questionTimeSpent: Map<Int, Int>,
    timeRemaining: Long,
    mockTest: MockTest
): TestAttempt {
    val attemptId = UUID.randomUUID().toString()
    val userAnswers = mutableMapOf<String, UserAnswer>()

    // Create user answers from selected options
    mockTest.questions.forEachIndexed { index, question ->
        // Include all questions in the attempt, with proper null handling
        val selectedOption = if (selectedOptions[index] == -1) null else selectedOptions[index]
        userAnswers[question.id] = UserAnswer(
            questionId = question.id,
            selectedOptionIndex = selectedOption,
            timeSpent = questionTimeSpent[index] ?: 0,
            status = questionStatus[index]
        )
    }

    val testDuration = (mockTest.timeLimit * 60) - timeRemaining.toInt()

    // Create the test attempt with proper null handling
    return TestAttempt(
        id = attemptId,
        testId = testId,
        startTime = Date(System.currentTimeMillis() - (testDuration * 1000L)),
        endTime = Date(),
        userAnswers = userAnswers,
        isCompleted = true
    )
}

fun calculateTimeSpentOnQuestion(questionIndex: Int, endTimeMillis: Long): Int {
    // Calculate the time spent on a specific question
    return ((endTimeMillis - (questionIndex * 60 * 1000L)) / 1000).toInt()
}
