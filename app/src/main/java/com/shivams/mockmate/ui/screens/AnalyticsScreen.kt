package com.shivams.mockmate.ui.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.components.AccuracyTrendChart
import com.shivams.mockmate.ui.components.AvgTimeSpentChart
import com.shivams.mockmate.ui.components.EngagementTimelineChart
import com.shivams.mockmate.ui.components.PerQuestionAnalysisChart
import com.shivams.mockmate.ui.components.StreakTrackerChart
import com.shivams.mockmate.ui.components.TestAttemptsCounterChart
import com.shivams.mockmate.ui.components.TestScoresOverTimeChart
import com.shivams.mockmate.ui.theme.MockMateTheme

@Composable
fun KotlinOverallAccuracyChart(userStats: UserStats, modifier: Modifier = Modifier) {
    val correct = userStats.correctAnswers
    val total = userStats.questionsAnswered
    val incorrect = total - correct
    val accuracyPercentage = if (total > 0) (correct.toFloat() / total.toFloat()) * 100 else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Accuracy", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sweepAngleCorrect = if (total > 0) (correct.toFloat() / total.toFloat()) * 360f else 0f
                    val sweepAngleIncorrect = if (total > 0) (incorrect.toFloat() / total.toFloat()) * 360f else 0f

                    drawArc(
                        color = Color.Red.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = sweepAngleIncorrect + sweepAngleCorrect, // Draw full circle if only incorrect or only correct
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    drawArc(
                        color = Color.Green.copy(alpha = 0.5f),
                        startAngle = -90f,
                        sweepAngle = sweepAngleCorrect,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    drawArc(
                        color = Color.Gray,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = true,
                        style = Stroke(width = 1.dp.toPx()),
                        size = Size(size.width, size.height)
                    )
                }
                Text(String.format("%.1f%%", accuracyPercentage), style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("Correct: $correct", color = Color.Green.copy(alpha = 0.8f))
                Text("Incorrect: $incorrect", color = Color.Red.copy(alpha = 0.8f))
            }
            Text("Total Questions: $total")
        }
    }
}


@Composable
fun AnalyticsScreen(
    userStats: UserStats,
    testAttempts: List<TestAttempt>,
) {
    Log.d("AnalyticsScreen", "UserStats received: questionsAnswered=${userStats.questionsAnswered}, correctAnswers=${userStats.correctAnswers}, currentStreak=${userStats.currentStreak}")
    Log.d("AnalyticsScreen", "SubjectPerformance size: ${userStats.subjectPerformance.size}")
    Log.d("AnalyticsScreen", "TestAttempts received: ${testAttempts.size} attempts")
    testAttempts.forEach { attempt ->
        Log.d("AnalyticsScreen", "Attempt ${attempt.id}: testId=${attempt.testId}, score=${attempt.score}, completed=${attempt.isCompleted}")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Analytics Screen", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 16.dp))

        KotlinOverallAccuracyChart(userStats = userStats)

        Text("Progress Over Time", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        TestScoresOverTimeChart(testAttempts = testAttempts)
        AccuracyTrendChart(testAttempts = testAttempts)
        EngagementTimelineChart(testAttempts = testAttempts)

        Text("Time Management Insights", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        AvgTimeSpentChart(userStats = userStats)
        PerQuestionAnalysisChart(testAttempts = testAttempts)

        Text("Engagement & Habits", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        StreakTrackerChart(userStats = userStats)
        TestAttemptsCounterChart(testAttempts = testAttempts)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    MockMateTheme {
        AnalyticsScreen(
            userStats = UserStats(),
            testAttempts = emptyList(),
        )
    }
}
