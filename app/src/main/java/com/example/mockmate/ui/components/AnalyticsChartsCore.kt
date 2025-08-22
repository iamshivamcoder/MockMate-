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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.UserStats
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChartPlaceholder(title: String, description: String) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chart: $description",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "(Note: Implement a charting library for a visual representation)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun OverallAccuracyChart(userStats: UserStats) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Accuracy (Pie/Donut Chart)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Correct vs Incorrect across all attempts.",
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
fun SubjectWiseAccuracyChart(userStats: UserStats) {
    val subjectPerformanceList = userStats.subjectPerformance.values.toList()

    val chartEntryModelProducer = remember(subjectPerformanceList) {
        ChartEntryModelProducer(
            subjectPerformanceList.mapIndexed { index, subjectPerf ->
                entryOf(index.toFloat(), subjectPerf.accuracy)
            }
        )
    }

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        subjectPerformanceList.getOrNull(value.toInt())?.subject ?: ""
    }
    
    val columnColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(250.dp) // Set a fixed height for the chart card
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Subject-wise Accuracy",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (subjectPerformanceList.isEmpty()) {
                Text(
                    text = "No subject performance data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Chart(
                    chart = columnChart( // Use the direct import
                         columns = currentChartStyle.columnChart.columns.mapIndexed { index, lineComponent ->
                            LineComponent(
                                color = columnColors[index % columnColors.size].hashCode(),
                                thicknessDp = lineComponent.thicknessDp,
                                shape = Shapes.roundedCornerShape(allPercent = 40),
                                dynamicShader = null,
                                margins = lineComponent.margins
                            )
                        }
                    ),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        title = "Accuracy (%)",
                        valueFormatter = { value, _ -> "%.0f".format(value) }
                    ),
                    bottomAxis = rememberBottomAxis(
                        title = "Subject",
                        valueFormatter = bottomAxisValueFormatter,
                        guideline = null
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TestScoresOverTimeChart(testAttempts: List<TestAttempt>) {
    val sortedAttempts = remember(testAttempts) {
        testAttempts.sortedBy { it.startTime.time }
    }

    val chartEntryModelProducer = remember(sortedAttempts) {
        ChartEntryModelProducer(
            sortedAttempts.mapIndexed { index, attempt ->
                entryOf(index.toFloat(), attempt.score) // Using score as Y value
            }
        )
    }

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        sortedAttempts.getOrNull(value.toInt())?.startTime?.let { dateFormat.format(it) } ?: ""
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(250.dp) // Set a fixed height for the chart card
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Test Scores Over Time",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (sortedAttempts.isEmpty()) {
                Text(
                    text = "No test scores available to display.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else {
                Chart(
                    chart = lineChart(), // Using a simple line chart
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        title = "Score (%)", // Assuming score is a percentage
                        valueFormatter = { value, _ -> "%.0f".format(value) }
                    ),
                    bottomAxis = rememberBottomAxis(
                        title = "Date",
                        valueFormatter = bottomAxisValueFormatter,
                        guideline = null
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TopicDrilldownChart(userStats: UserStats) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Topic Drilldown", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Data from ${userStats.subjectPerformance.size} subjects.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "(Placeholder: Visual chart for topic drilldown needed)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun AccuracyTrendChart(testAttempts: List<TestAttempt>) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Accuracy Trend", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (testAttempts.isNotEmpty()) {
                val avgScore = testAttempts.map { it.score }.average()
                Text(
                    text = "Average score over ${testAttempts.size} attempts: %.2f%%".format(avgScore),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "No test attempts to show trend.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "(Placeholder: Visual chart for accuracy trend needed)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun EngagementTimelineChart(testAttempts: List<TestAttempt>) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Engagement Timeline", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (testAttempts.isNotEmpty()) {
                val firstAttemptDate = testAttempts.minOfOrNull { it.startTime }?.let { dateFormat.format(it) } ?: "N/A"
                val lastAttemptDate = testAttempts.maxOfOrNull { it.startTime }?.let { dateFormat.format(it) } ?: "N/A"
                Text(
                    text = "${testAttempts.size} attempts recorded.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Activity from: $firstAttemptDate to $lastAttemptDate",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "No engagement data available.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "(Placeholder: Visual calendar heatmap for engagement needed)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun PerQuestionAnalysisChart(testAttempts: List<TestAttempt>) {
     Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Per Question Analysis", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
             if (testAttempts.isNotEmpty()) {
                Text(
                    text = "Analysis based on ${testAttempts.size} attempts.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "No attempts available for question analysis.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "(Placeholder: Detailed per-question chart needed)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
