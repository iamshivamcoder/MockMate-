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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateContentSize
import com.shivams.mockmate.ui.components.AnswerFeedbackCard
import com.shivams.mockmate.ui.components.SwipeableStatementCard
import com.shivams.mockmate.ui.components.TrueFalseActionButtons
import com.shivams.mockmate.ui.components.TrueFalseProgressIndicator
import com.shivams.mockmate.ui.viewmodels.TrueFalseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseSessionScreen(
    viewModel: TrueFalseViewModel,
    sessionId: String,
    onNavigateBack: () -> Unit,
    onSessionComplete: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    val currentStatement = uiState.currentStatement
    
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${uiState.session?.topic ?: "Practice"} - ${uiState.session?.subject ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = formatTimeSession(uiState.sessionTimeElapsed),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Stop, contentDescription = "Finish", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        // Use Box as root to avoid ColumnScope.AnimatedVisibility issues
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content column
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TrueFalseProgressIndicator(
                    current = uiState.currentIndex,
                    total = uiState.statements.size,
                    correctCount = uiState.userAnswers.count { (id, answer) ->
                        answer != null && uiState.statements.find { it.id == id }?.isTrue == answer
                    },
                    incorrectCount = uiState.userAnswers.count { (id, answer) ->
                        answer != null && uiState.statements.find { it.id == id }?.isTrue != answer
                    },
                    skippedCount = uiState.userAnswers.count { it.value == null }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Main content area - using key to force recomposition on answer state change
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    if (currentStatement != null) {
                        val isAnswered = uiState.userAnswers.containsKey(currentStatement.id)
                        
                        when {
                            // Show swipeable card only if not answered and not showing explanation
                            !isAnswered && !uiState.showExplanation -> {
                                SwipeableStatementCard(
                                    statement = currentStatement.statement,
                                    onSwipeRight = { viewModel.answerTrue() },
                                    onSwipeLeft = { viewModel.answerFalse() },
                                    onSwipeUp = { viewModel.skipStatement() }
                                )
                            }
                            // When showing explanation, don't show anything here - the overlay handles it
                            uiState.showExplanation -> {
                                // Empty - AnswerFeedbackCard overlay will show at bottom
                            }
                            // Show answered state only when answered AND not showing explanation
                            isAnswered && !uiState.showExplanation -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentStatement.statement,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    val userAnswer = uiState.userAnswers[currentStatement.id]
                                    val isCorrect = userAnswer == currentStatement.isTrue
                                    Text(
                                        text = when {
                                            userAnswer == null -> "Skipped"
                                            userAnswer -> "You answered: TRUE"
                                            else -> "You answered: FALSE"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = when {
                                            userAnswer == null -> Color(0xFFFF9800)
                                            isCorrect -> Color(0xFF4CAF50)
                                            else -> Color(0xFFF44336)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Action buttons at bottom
                if (!uiState.showExplanation) {
                    val isCurrentAnswered = currentStatement?.let {
                        uiState.userAnswers.containsKey(it.id)
                    } ?: false
                    
                    if (!isCurrentAnswered) {
                        TrueFalseActionButtons(
                            onTrue = { viewModel.answerTrue() },
                            onFalse = { viewModel.answerFalse() },
                            onSkip = { viewModel.skipStatement() }
                        )
                    } else {
                        // Navigation buttons after answer - with key for proper recomposition
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .animateContentSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { viewModel.previousStatement() },
                                enabled = uiState.canGoPrevious
                            ) {
                                Text("Previous")
                            }
                            if (uiState.isLastStatement) {
                                Button(onClick = {
                                    viewModel.finishSession()
                                    uiState.session?.id?.let { onSessionComplete(it) }
                                }) {
                                    Text("Finish")
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.nextStatement() },
                                    enabled = uiState.canGoNext
                                ) {
                                    Text("Next")
                                }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.dismissExplanation()
                            if (uiState.isLastStatement) {
                                viewModel.finishSession()
                                uiState.session?.id?.let { onSessionComplete(it) }
                            } else {
                                viewModel.nextStatement()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp)
                    ) {
                        Text(
                            text = if (uiState.isLastStatement) "View Results" else "Next Statement",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Feedback overlay at bottom - using if statement instead of AnimatedVisibility
            if (uiState.showExplanation && uiState.lastAnswerResult != null) {
                uiState.lastAnswerResult?.let { result ->
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        AnswerFeedbackCard(
                            isCorrect = result.isCorrect,
                            explanation = result.explanation,
                            trapWords = result.trapWords,
                            upscTip = result.upscTip,
                            onDismiss = {
                                viewModel.dismissExplanation()
                                if (uiState.isLastStatement) {
                                    viewModel.finishSession()
                                    uiState.session?.id?.let { onSessionComplete(it) }
                                } else {
                                    viewModel.nextStatement()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("End Session?") },
            text = {
                Text("You've answered ${uiState.answeredCount} of ${uiState.statements.size} statements. Do you want to end the session now?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishSession()
                        uiState.session?.id?.let { onSessionComplete(it) }
                        showExitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("End & View Results")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continue")
                }
            }
        )
    }
}

private fun formatTimeSession(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
