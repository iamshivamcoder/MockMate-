package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.AttemptWithTest
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.TestHistoryContent
import com.shivams.mockmate.ui.viewmodels.TestHistoryViewModel

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
    viewModel: TestHistoryViewModel = hiltViewModel()
) {
    val testAttemptsFlow = repository.getAllTestAttempts()
    val testAttemptsList by testAttemptsFlow.collectAsState(initial = emptyList())
    val userStats by repository.userStats.collectAsState(initial = UserStats())

    var isLoading by remember { mutableStateOf(true) }
    var attemptsWithTest by remember { mutableStateOf<List<AttemptWithTest>>(emptyList()) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    var showRenameDialog by remember { mutableStateOf<AttemptWithTest?>(null) }
    var showDeleteDialog by remember { mutableStateOf<AttemptWithTest?>(null) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(testAttemptsList) {
        isLoading = true
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            TestHistoryContent(
                modifier = Modifier.padding(innerPadding),
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

    showRenameDialog?.let { attempt ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Test") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New test name") })
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.renameTestAttempt(attempt.attemptId, newName)
                    showRenameDialog = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    showDeleteDialog?.let { attempt ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Attempt") },
            text = { Text("Are you sure you want to permanently delete this test attempt?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteTestAttempt(attempt.attemptId)
                    showDeleteDialog = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
