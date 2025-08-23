package com.example.mockmate.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.UserStats
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun AvgTimeSpentChart(userStats: UserStats) {
    // Debug logging
    android.util.Log.d("AvgTimeSpentChart", "UserStats received: questionsAnswered=${userStats.questionsAnswered}, subjectPerformance=${userStats.subjectPerformance.size}")

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊 Time Management Insights", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (userStats.questionsAnswered == 0) {
                // No data state
                Text(
                    text = "Start small today. Even 15 mins counts. ⏱️",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Complete practice tests to see your time management insights",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Show basic stats since we don't have actual time tracking yet
                val avgQuestions = if (userStats.subjectPerformance.isNotEmpty()) {
                    userStats.questionsAnswered.toFloat() / userStats.subjectPerformance.size
                } else 0f

                val accuracy = if (userStats.questionsAnswered > 0) {
                    (userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100
                } else 0f

                Text("Questions answered: ${userStats.questionsAnswered}", style = MaterialTheme.typography.bodyMedium)
                Text("Avg. per subject: %.1f".format(avgQuestions), style = MaterialTheme.typography.bodyMedium)
                Text("Current accuracy: %.1f%%".format(accuracy), style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📈 Focus on quality over speed - accuracy matters more than time!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun StreakTrackerChart(userStats: UserStats) {
    ChartPlaceholder(
        title = "Streak Tracker (Flame/Fire Progress Bar)",
        description = "Current streak shown with visual fire icons 🔥."
    )
}

@Composable
fun TestAttemptsCounterChart(testAttempts: List<TestAttempt>) {
    // Milestone Chart: Simple bar chart for number of attempts
    val milestones = listOf(5, 10, 20, 50)
    val entryModel = ChartEntryModelProducer(
        milestones.mapIndexed { index, milestone ->
            entryOf(index.toFloat(), if (testAttempts.size >= milestone) milestone.toFloat() else 0f)
        }
    )
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier
        .padding(vertical = 8.dp)
        .fillMaxWidth()
        .height(220.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Test Attempts Counter (Badge/Level System)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Chart(
                chart = columnChart(),
                chartModelProducer = entryModel,
                startAxis = rememberStartAxis(title = "Tests Completed", valueFormatter = { v,_ -> v.toInt().toString() }),
                bottomAxis = rememberBottomAxis(title = "Milestone", valueFormatter={v,_->milestones.getOrNull(v.toInt())?.toString()?:""}),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
