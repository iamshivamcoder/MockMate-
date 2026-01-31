package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.ui.viewmodels.TrueFalseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseResultScreen(
    viewModel: TrueFalseViewModel,
    onRetry: () -> Unit,
    onDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Complete") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScoreCard(
                    score = uiState.finalScore,
                    totalPossible = uiState.statements.size * 2f,
                    correctCount = uiState.correctCount,
                    incorrectCount = uiState.incorrectCount,
                    skippedCount = uiState.skippedCount,
                    totalStatements = uiState.statements.size,
                    timeElapsed = uiState.sessionTimeElapsed
                )
            }
            
            item { Text("Statement Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            
            itemsIndexed(uiState.statements) { index, statement ->
                val userAnswer = uiState.userAnswers[statement.id]
                val isCorrect = userAnswer == statement.isTrue
                val isSkipped = userAnswer == null
                
                StatementResultCard(index + 1, statement.statement, statement.isTrue, userAnswer, isCorrect, isSkipped, statement.explanation, statement.trapWords, statement.upscTip)
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { viewModel.resetSession(); onRetry() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Session")
                    }
                    Button(onClick = onDashboard, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dashboard")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ScoreCard(score: Float, totalPossible: Float, correctCount: Int, incorrectCount: Int, skippedCount: Int, totalStatements: Int, timeElapsed: Int) {
    val accuracy = if (totalStatements > 0) (correctCount.toFloat() / totalStatements * 100).toInt() else 0
    val scoreColor = when { accuracy >= 70 -> Color(0xFF4CAF50); accuracy >= 40 -> Color(0xFFFF9800); else -> Color(0xFFF44336) }
    
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${score.toInt()}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = scoreColor, fontSize = 64.sp)
            Text("of ${totalPossible.toInt()} marks", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = scoreColor.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)) {
                Text("$accuracy% Accuracy", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = scoreColor)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(correctCount.toString(), "Correct", Color(0xFF4CAF50))
                StatItem(incorrectCount.toString(), "Incorrect", Color(0xFFF44336))
                StatItem(skippedCount.toString(), "Skipped", Color(0xFFFF9800))
                StatItem(formatTime(timeElapsed), "Time", MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatementResultCard(index: Int, statement: String, isTrue: Boolean, userAnswer: Boolean?, isCorrect: Boolean, isSkipped: Boolean, explanation: String, trapWords: List<String>, upscTip: String) {
    val backgroundColor = when { isSkipped -> Color(0xFFFF9800).copy(alpha = 0.1f); isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.1f); else -> Color(0xFFF44336).copy(alpha = 0.1f) }
    val statusIcon = when { isSkipped -> Icons.Default.SkipNext; isCorrect -> Icons.Default.Check; else -> Icons.Default.Close }
    val statusColor = when { isSkipped -> Color(0xFFFF9800); isCorrect -> Color(0xFF4CAF50); else -> Color(0xFFF44336) }
    
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = backgroundColor), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Q$index", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(statusColor), contentAlignment = Alignment.Center) {
                        Icon(statusIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(when { isSkipped -> "Skipped"; isCorrect -> "Correct"; else -> "Incorrect" }, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = statusColor)
                        if (!isSkipped) Text("You: ${if (userAnswer == true) "True" else "False"} | Correct: ${if (isTrue) "True" else "False"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(statement, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = if (isTrue) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFF44336).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                Text("Answer: ${if (isTrue) "TRUE" else "FALSE"}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isTrue) Color(0xFF4CAF50) else Color(0xFFF44336))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (trapWords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trap: ${trapWords.joinToString(", ")}", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFF9800))
                }
            }
            if (upscTip.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(upscTip, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String { val mins = seconds / 60; val secs = seconds % 60; return "%02d:%02d".format(mins, secs) }
