package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mockmate.model.UserStats
import com.example.mockmate.ui.theme.MockMateTheme
import java.util.Date

@Composable
fun OverallAccuracyChart(userStats: UserStats) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Accuracy", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // Placeholder for Pie/Donut Chart
            Text(
                text = "Chart: Display a pie/donut chart here.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            val accuracy = if (userStats.questionsAnswered > 0) {
                (userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100
            } else {
                0f
            }
            Text(
                text = "Correct: ${userStats.correctAnswers}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Incorrect: ${userStats.questionsAnswered - userStats.correctAnswers}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total Answered: ${userStats.questionsAnswered}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Accuracy: %.2f%%".format(accuracy),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "(Note: Implement a charting library for a visual pie/donut chart)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun AnalyticsScreen(
    userStats: UserStats, // Added UserStats as a parameter
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Analytics Screen", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OverallAccuracyChart(userStats = userStats)
        // TODO: Add more charts here based on the list
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    MockMateTheme {
        // Preview with sample data
        val sampleUserStats = UserStats(
            questionsAnswered = 100,
            correctAnswers = 75,
            streak = 5,
            lastPracticeDate = Date(),
            subjectPerformance = emptyMap()
        )
        AnalyticsScreen(userStats = sampleUserStats, onNavigateBack = {})
    }
}
