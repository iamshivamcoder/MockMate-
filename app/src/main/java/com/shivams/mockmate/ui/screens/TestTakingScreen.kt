package com.shivams.mockmate.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // Added for loading
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shivams.mockmate.data.SettingsRepository
import com.shivams.mockmate.data.TestRepository
import com.shivams.mockmate.model.AppSettings // Required for default settings
import com.shivams.mockmate.model.Question
import com.shivams.mockmate.model.QuestionStatus // Ensure this is imported
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.OptionItem
import com.shivams.mockmate.ui.components.QuestionDifficultyBadge
import com.shivams.mockmate.ui.components.TestProgressHeader // Import the extracted component
import com.shivams.mockmate.ui.viewmodels.TestTakingScreenViewModel
import com.shivams.mockmate.ui.components.FinishTestDialog
import com.shivams.mockmate.ui.components.ErrorAlertDialog
import com.shivams.mockmate.data.generateSampleQuestionPreview // Added import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestTakingScreen(
    testId: String,
    onNavigateBack: () -> Unit,
    onFinish: (attemptId: String) -> Unit,
    repository: TestRepository,
    settingsRepository: SettingsRepository = SettingsRepository(LocalContext.current)
) {
    val viewModel: TestTakingScreenViewModel = viewModel(
        factory = TestTakingScreenViewModel.provideFactory(repository, testId)
    )

    val uiState by viewModel.uiState.collectAsState()
    val appSettings by settingsRepository.settings.collectAsState(initial = AppSettings())

    var showFinishDialog by remember { mutableStateOf(false) }
    val questionStartTimes = remember { mutableMapOf<Int, Long>() }
    var previousQuestionIndex by remember(uiState.currentQuestionIndex) { mutableIntStateOf(uiState.currentQuestionIndex) }


    LaunchedEffect(uiState.timeRemaining, uiState.mockTest) {
        if (uiState.timeRemaining <= 0 && uiState.mockTest != null) {
            showFinishDialog = true
        }
    }

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading Test...")
        }
        return
    }

    if (uiState.mockTest == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            uiState.errorMessage?.let { ErrorAlertDialog(onDismiss = { viewModel.clearError() }, errorMessage = it) } 
                ?: Text("Test not found or failed to load.")
            Button(onClick = onNavigateBack) {
                Text("Go Back")
            }
        }
        return
    }
    
    val currentQuestionIndex = uiState.currentQuestionIndex
    val mockTest = uiState.mockTest!!

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
    LaunchedEffect(Unit) {
        if (!questionStartTimes.containsKey(currentQuestionIndex)) {
            questionStartTimes[currentQuestionIndex] = System.currentTimeMillis()
        }
    }

    if (currentQuestionIndex < 0 || currentQuestionIndex >= mockTest.questions.size) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Error: Invalid question index.")
            Button(onClick = onNavigateBack) {
                Text("Go Back")
            }
        }
        return
    }
    val currentQuestion = mockTest.questions[currentQuestionIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        MockMateTopBar(
            title = mockTest.name,
            onBackClick = { showFinishDialog = true }
        )

        TestProgressHeader(
            currentQuestion = currentQuestionIndex + 1,
            totalQuestions = mockTest.questions.size,
            timeRemaining = uiState.timeRemaining
        )

        QuestionContent(
            modifier = Modifier.weight(1f),
            question = currentQuestion,
            selectedOptionIndex = uiState.selectedOptions.getOrElse(currentQuestionIndex) { -1 },
            onOptionSelected = { optionIndex ->
                viewModel.updateSelectedOption(currentQuestionIndex, optionIndex)
                if (optionIndex != -1) {
                    viewModel.updateQuestionStatus(currentQuestionIndex, QuestionStatus.ANSWERED)
                }
            },
            pulsateBadges = appSettings.pulsatingBadgesEnabled
        )

        TestNavigationFooter(
            currentQuestionIndex = currentQuestionIndex,
            totalQuestions = mockTest.questions.size,
            onPreviousClick = { viewModel.moveToPreviousQuestion() },
            onNextClick = { viewModel.moveToNextQuestion() },
            onFinishClick = { showFinishDialog = true },
            onBookmarkClick = {
                viewModel.toggleBookmark(currentQuestionIndex)
            },
            onMarkForReviewClick = {
                viewModel.toggleMarkForReview(currentQuestionIndex)
            },
            isBookmarked = uiState.questionStatus.getOrElse(currentQuestionIndex) { QuestionStatus.UNATTEMPTED } == QuestionStatus.BOOKMARKED,
            isMarkedForReview = uiState.questionStatus.getOrElse(currentQuestionIndex) { QuestionStatus.UNATTEMPTED } == QuestionStatus.MARKED_FOR_REVIEW
        )
    }

    if (showFinishDialog) {
        questionStartTimes[currentQuestionIndex]?.let { startTime ->
            val timeSpent = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            viewModel.updateQuestionTimeSpent(currentQuestionIndex, timeSpent)
            questionStartTimes.remove(currentQuestionIndex)
        }

        val attemptedCount = uiState.selectedOptions.count { it != -1 }

        if (!uiState.isSaving) {
            FinishTestDialog(
                onDismiss = { showFinishDialog = false },
                onConfirm = {
                    viewModel.submitTestAttempt { attemptId ->
                        onFinish(attemptId)
                    }
                },
                attemptedQuestions = attemptedCount,
                totalQuestions = mockTest.questions.size
            )
        }
    }

    uiState.errorMessage?.let { message ->
        android.util.Log.e("TestTakingScreen", "Error dialog shown: $message")
        ErrorAlertDialog(
            onDismiss = { viewModel.clearError() },
            errorMessage = message
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TestTakingScreenPreview() {
    val context = LocalContext.current
    TestTakingScreen(
        testId = "sampleTestId",
        onNavigateBack = {},
        onFinish = {},
        repository = com.shivams.mockmate.data.InMemoryTestRepository(),
        settingsRepository = SettingsRepository(context)
    )
}

// TestProgressHeader and its Preview have been moved to ProgressComponents.kt
// QuestionContent and its Preview remain here for now, or could be moved similarly.
// TestNavigationFooter and its Preview remain here for now, or could be moved similarly.
// FinishTestDialog and its Preview have been removed.
// formatTime has been moved to ProgressComponents.kt

@Composable
fun QuestionContent(
    modifier: Modifier = Modifier,
    question: Question,
    selectedOptionIndex: Int,
    onOptionSelected: (Int) -> Unit,
    pulsateBadges: Boolean
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionDifficultyBadge(
                difficulty = question.difficulty,
                isPulsating = pulsateBadges
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${question.subject} > ${question.topic}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Text(
            text = "Select an answer:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        question.options?.forEachIndexed { index, option ->
            OptionItem(
                optionText = option,
                selected = selectedOptionIndex == index,
                onOptionClick = { onOptionSelected(index) }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun QuestionContentPreview() {
    QuestionContent(
        question = generateSampleQuestionPreview(), // Use function from SampleData.kt
        selectedOptionIndex = -1,
        onOptionSelected = {},
        pulsateBadges = true
    )
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

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TestNavigationFooterPreview() {
    TestNavigationFooter(
        currentQuestionIndex = 0,
        totalQuestions = 10,
        onPreviousClick = {},
        onNextClick = {},
        onFinishClick = {},
        onBookmarkClick = {},
        onMarkForReviewClick = {},
        isBookmarked = false,
        isMarkedForReview = true
    )
}

