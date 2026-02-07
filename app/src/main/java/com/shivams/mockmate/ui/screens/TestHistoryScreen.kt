package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.AttemptWithTest
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.components.DeleteConfirmationDialog
import com.shivams.mockmate.ui.components.HistoryEmptyState
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.RenameDialog
import com.shivams.mockmate.ui.components.TestHistoryContent
import com.shivams.mockmate.ui.viewmodels.TestHistoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TestHistorySortCriteria {
    DATE,
    SCORE,
    TEST_NAME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestHistoryScreen(
    onNavigateBack: () -> Unit,
    onViewTestResult: (String, String) -> Unit,
    repository: TestRepository,
    viewModel: TestHistoryViewModel = hiltViewModel(),
    onStartMock: (() -> Unit)? = null
) {
    val testAttemptsFlow = repository.getAllTestAttempts()
    val testAttemptsList by testAttemptsFlow.collectAsState(initial = emptyList())
    val userStats by repository.userStats.collectAsState(initial = UserStats())

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var attemptsWithTest by remember { mutableStateOf<List<AttemptWithTest>>(emptyList()) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    var showRenameDialog by remember { mutableStateOf<AttemptWithTest?>(null) }
    var showDeleteDialog by remember { mutableStateOf<AttemptWithTest?>(null) }
    var newName by remember { mutableStateOf("") }

    suspend fun processAttempts() {
        val processedAttempts = testAttemptsList.mapNotNull { attempt ->
            val test = repository.getTestById(attempt.testId)
            if (test != null) {
                val attemptedQuestions = attempt.userAnswers.size
                val correctAnswers = attempt.userAnswers.count { (questionId, userAnswer) ->
                    val question = test.questions.find { it.id == questionId }
                    question != null && userAnswer.selectedOptionIndex == question.correctOptionIndex
                }
                AttemptWithTest(
                    attemptId = attempt.id,
                    testId = attempt.testId,
                    testName = attempt.customName ?: test.name,
                    date = attempt.startTime,
                    attemptedQuestions = attemptedQuestions,
                    correctAnswers = correctAnswers,
                    totalQuestions = test.questions.size,
                    score = correctAnswers
                )
            } else {
                null
            }
        }
        attemptsWithTest = processedAttempts
    }

    LaunchedEffect(testAttemptsList) {
        isLoading = true
        processAttempts()
        isLoading = false
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MockMateTopBar(
                title = "Test History & Analytics",
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    delay(500) // Brief delay for visual feedback
                    processAttempts()
                    isRefreshing = false
                }
            },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                attemptsWithTest.isEmpty() -> {
                    HistoryEmptyState(
                        onStartMock = onStartMock ?: {}
                    )
                }
                else -> {
                    TestHistoryContent(
                        modifier = Modifier,
                        userStats = userStats,
                        testAttempts = attemptsWithTest,
                        onViewResult = { attemptId, testId ->
                            onViewTestResult(attemptId, testId)
                        },
                        onRenameClick = { 
                            newName = it.testName
                            showRenameDialog = it
                        },
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }
            }
        }
    }

    showRenameDialog?.let { attempt ->
        RenameDialog(
            currentName = newName,
            onDismiss = { showRenameDialog = null },
            onConfirm = { name ->
                viewModel.renameTestAttempt(attempt.attemptId, name)
                showRenameDialog = null
            },
            title = "Rename Test",
            label = "New test name"
        )
    }

    showDeleteDialog?.let { attempt ->
        DeleteConfirmationDialog(
            itemName = attempt.testName,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteTestAttempt(attempt.attemptId)
                showDeleteDialog = null
            },
            title = "Delete Test Attempt"
        )
    }
}

