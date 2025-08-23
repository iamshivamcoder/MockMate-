package com.example.mockmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
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

            // Debug logging for data validation
            android.util.Log.d("OverallAccuracyChart", "Received userStats: questionsAnswered=${userStats.questionsAnswered}, correctAnswers=${userStats.correctAnswers}")
            android.util.Log.d("OverallAccuracyChart", "Calculated accuracy: ${if (userStats.questionsAnswered > 0) "%.2f".format((userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100) else "No data"}")
            Spacer(modifier = Modifier.height(8.dp))
            val accuracy = if (userStats.questionsAnswered > 0) {
                (userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100
            } else {
                0f
            }
            // Pie chart visualization
            if (userStats.questionsAnswered > 0) {
                val correctPercentage = (userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100
                val incorrectPercentage = 100 - correctPercentage

                Text("🥧 Accuracy Breakdown:", style = MaterialTheme.typography.titleSmall)
                Text("✅ Correct: %.1f%%".format(correctPercentage), style = MaterialTheme.typography.bodyMedium)
                Text("❌ Incorrect: %.1f%%".format(incorrectPercentage), style = MaterialTheme.typography.bodyMedium)

                // Enhanced visual progress bar
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth(correctPercentage / 100f)
                            .height(32.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            )
                    )
                }

                // Legend
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    // Correct indicator
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = colorScheme.primary,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Text(" Correct ", style = MaterialTheme.typography.labelSmall)
                    }

                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))

                    // Incorrect indicator
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = colorScheme.error,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Text(" Incorrect ", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Text(
                text = "Total Answered: ${userStats.questionsAnswered}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Accuracy: ${if (userStats.questionsAnswered > 0) "%.2f%%".format(accuracy) else "No data"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SubjectWiseAccuracyChart(userStats: UserStats) {
    val subjectPerformanceList = userStats.subjectPerformance.values.toList()

    // Debug logging for subject performance
    android.util.Log.d("SubjectWiseAccuracyChart", "Subject performance list size: ${subjectPerformanceList.size}")
    subjectPerformanceList.forEach { perf ->
        android.util.Log.d("SubjectWiseAccuracyChart", "Subject: ${perf.subject}, Accuracy: ${perf.accuracy}%, Questions: ${perf.questionsAttempted}, Correct: ${perf.correctAnswers}")
    }

    val chartEntryModelProducer = remember(subjectPerformanceList) {
        ChartEntryModelProducer(
            subjectPerformanceList.mapIndexed { index, subjectPerf ->
                entryOf(index.toFloat(), subjectPerf.accuracy.toFloat())
            }
        )
    }

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        subjectPerformanceList.getOrNull(value.toInt())?.subject ?: ""
    }
    
    val columnColors = listOf(
        colorScheme.primary,
        colorScheme.secondary,
        colorScheme.tertiary,
        colorScheme.primaryContainer,
        colorScheme.secondaryContainer
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(280.dp), // Increased height for better visibility
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📚 Subject-wise Accuracy",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (subjectPerformanceList.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📚 No subject performance data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Complete practice tests to see your subject-wise accuracy breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
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

    // Debug logging for test attempts
    android.util.Log.d("TestScoresOverTimeChart", "Test attempts list size: ${testAttempts.size}")
    sortedAttempts.forEach { attempt ->
        android.util.Log.d("TestScoresOverTimeChart", "Attempt ${attempt.id}: score=${attempt.score}, startTime=${attempt.startTime}, completed=${attempt.isCompleted}")
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
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📈 Test Scores Over Time",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (sortedAttempts.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📈 No test scores available to display",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Take practice tests to track your progress over time",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
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
    val sortedAttempts = remember(testAttempts) {
        testAttempts.sortedBy { it.startTime.time }
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📈 Accuracy Trend", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (sortedAttempts.isEmpty()) {
                Text(
                    text = "No test attempts to show trend.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                val chartEntryModelProducer = remember(sortedAttempts) {
                    ChartEntryModelProducer(
                        sortedAttempts.mapIndexed { index, attempt ->
                            entryOf(index.toFloat(), attempt.score.toFloat())
                        }
                    )
                }

                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    sortedAttempts.getOrNull(value.toInt())?.startTime?.let { dateFormat.format(it) } ?: ""
                }

                Chart(
                    chart = lineChart(),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        title = "Score (%)",
                        valueFormatter = { value, _ -> "%.0f".format(value) }
                    ),
                    bottomAxis = rememberBottomAxis(
                        title = "Test Attempts",
                        valueFormatter = bottomAxisValueFormatter,
                        guideline = null
                    ),
                    modifier = Modifier.fillMaxSize()
                )

                val avgScore = sortedAttempts.map { it.score }.average()
                Text(
                    text = "Average: %.1f%% over ${sortedAttempts.size} attempts".format(avgScore),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EngagementTimelineChart(testAttempts: List<TestAttempt>) {
    val sortedAttempts = remember(testAttempts) {
        testAttempts.sortedBy { it.startTime.time }
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📅 Engagement Timeline", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (sortedAttempts.isEmpty()) {
                Text(
                    text = "No engagement data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Create timeline data - attempts per day
                val attemptsByDay = sortedAttempts.groupBy { attempt ->
                    val cal = java.util.Calendar.getInstance().apply {
                        time = attempt.startTime
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis
                }

                val timelineEntries = attemptsByDay.entries.sortedBy { it.key }.takeLast(7) // Last 7 days

                val chartEntryModelProducer = remember(timelineEntries) {
                    ChartEntryModelProducer(
                        timelineEntries.mapIndexed { index, entry ->
                            entryOf(index.toFloat(), entry.value.size.toFloat())
                        }
                    )
                }

                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    timelineEntries.getOrNull(value.toInt())?.key?.let {
                        dateFormat.format(java.util.Date(it))
                    } ?: ""
                }

                Chart(
                    chart = columnChart(
                        columns = listOf(
                            com.patrykandpatrick.vico.core.component.shape.LineComponent(
                                color = colorScheme.secondary.hashCode(),
                                thicknessDp = 8f,
                                shape = Shapes.roundedCornerShape(allPercent = 20)
                            )
                        )
                    ),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        title = "Tests",
                        valueFormatter = { value, _ -> "%.0f".format(value) }
                    ),
                    bottomAxis = rememberBottomAxis(
                        title = "Date",
                        valueFormatter = bottomAxisValueFormatter,
                        guideline = null
                    ),
                    modifier = Modifier.fillMaxSize()
                )

                val totalAttempts = testAttempts.size
                val firstAttemptDate = sortedAttempts.firstOrNull()?.startTime?.let { dateFormat.format(it) } ?: "N/A"
                val lastAttemptDate = sortedAttempts.lastOrNull()?.startTime?.let { dateFormat.format(it) } ?: "N/A"

                Text(
                    text = "$totalAttempts tests ($firstAttemptDate - $lastAttemptDate)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PerQuestionAnalysisChart(testAttempts: List<TestAttempt>) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊 Per Question Analysis", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (testAttempts.isEmpty()) {
                Text(
                    text = "No attempts available for question analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Analyze score distribution
                val scores = testAttempts.map { it.score }
                val avgScore = scores.average()
                val minScore = scores.minOrNull() ?: 0f
                val maxScore = scores.maxOrNull() ?: 0f

                // Create score distribution buckets
                val scoreRanges = listOf(0f, 25f, 50f, 75f, 100f)
                val distribution = mutableListOf<Int>()
                for (i in 0 until scoreRanges.size - 1) {
                    val count = scores.count { it >= scoreRanges[i] && it < scoreRanges[i + 1] }
                    distribution.add(count)
                }

                val chartEntryModelProducer = remember(distribution) {
                    ChartEntryModelProducer(
                        distribution.mapIndexed { index, count ->
                            entryOf(index.toFloat(), count.toFloat())
                        }
                    )
                }

                val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    when (value.toInt()) {
                        0 -> "0-25%"
                        1 -> "25-50%"
                        2 -> "50-75%"
                        3 -> "75-100%"
                        else -> ""
                    }
                }

                Chart(
                    chart = columnChart(
                        columns = listOf(
                            com.patrykandpatrick.vico.core.component.shape.LineComponent(
                                color = colorScheme.tertiary.hashCode(),
                                thicknessDp = 8f,
                                shape = Shapes.roundedCornerShape(allPercent = 20)
                            )
                        )
                    ),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        title = "Tests",
                        valueFormatter = { value, _ -> "%.0f".format(value) }
                    ),
                    bottomAxis = rememberBottomAxis(
                        title = "Score Range",
                        valueFormatter = bottomAxisValueFormatter,
                        guideline = null
                    ),
                    modifier = Modifier.fillMaxSize()
                )

                // Summary statistics
                Text(
                    text = "📈 Avg: %.1f%% | 📉 Min: %.1f%% | 📈 Max: %.1f%%".format(avgScore, minScore, maxScore),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
