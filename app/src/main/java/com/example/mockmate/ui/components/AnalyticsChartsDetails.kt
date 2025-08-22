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
    // Example implementation: Display total questions answered over total subjects as a proxy
    val avgQuestions = if (userStats.subjectPerformance.isNotEmpty()) {
        userStats.questionsAnswered.toFloat() / userStats.subjectPerformance.size
    } else 0f
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Avg Time Spent vs Recommended", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Avg. answered per subject: %.2f".format(avgQuestions), style = MaterialTheme.typography.bodySmall)
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

@Composable
fun DifficultyBreakdownChart(userStats: UserStats) {
    // Example: Grouped Bar Chart for each subject by difficulty (proxy: accuracy)
    val subjects = userStats.subjectPerformance.values.toList()
    val entryModel = ChartEntryModelProducer(
        subjects.mapIndexed { idx, subj -> entryOf(idx.toFloat(), subj.accuracy) }
    )
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier
        .padding(vertical = 8.dp)
        .fillMaxWidth()
        .height(220.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Difficulty Breakdown", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Chart(
                chart = columnChart(),
                chartModelProducer = entryModel,
                startAxis = rememberStartAxis(title = "Accuracy (%)", valueFormatter = { v,_ -> "%.0f".format(v)}),
                bottomAxis = rememberBottomAxis(title = "Subject", valueFormatter={v,_->subjects.getOrNull(v.toInt())?.subject?:""}),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun SubjectDifficultyMatrixChart(userStats: UserStats) {
    // Proxy: Just accuracy per subject chart (real heatmap needs more data structure)
    val subjectsList = userStats.subjectPerformance.values.toList()
    val entryModel = ChartEntryModelProducer(
        subjectsList.mapIndexed { idx, subj -> entryOf(idx.toFloat(), subj.accuracy) }
    )
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Subject Difficulty Matrix", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Chart(
                chart = columnChart(),
                chartModelProducer = entryModel,
                startAxis = rememberStartAxis(title = "Accuracy (%)", valueFormatter = { v,_ -> "%.0f".format(v)}),
                bottomAxis = rememberBottomAxis(title = "Subject", valueFormatter={v,_->subjectsList.getOrNull(v.toInt())?.subject?:""}),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}