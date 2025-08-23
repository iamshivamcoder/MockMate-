package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.util.Log
import com.example.mockmate.model.SubjectPerformance
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.UserStats
import com.example.mockmate.ui.components.AccuracyTrendChart
import com.example.mockmate.ui.components.AvgTimeSpentChart
// import com.example.mockmate.ui.components.DifficultyBreakdownChart // Removed as per request
import com.example.mockmate.ui.components.EngagementTimelineChart
import com.example.mockmate.ui.components.OverallAccuracyChart
import com.example.mockmate.ui.components.PerQuestionAnalysisChart
import com.example.mockmate.ui.components.StreakTrackerChart
// import com.example.mockmate.ui.components.SubjectDifficultyMatrixChart // Removed as per request
import com.example.mockmate.ui.components.SubjectWiseAccuracyChart
import com.example.mockmate.ui.components.TestAttemptsCounterChart
import com.example.mockmate.ui.components.TestScoresOverTimeChart
import com.example.mockmate.ui.components.TopicDrilldownChart
import com.example.mockmate.ui.theme.MockMateTheme
import java.util.Date

@Composable
fun AnalyticsScreen(
    userStats: UserStats,
    testAttempts: List<TestAttempt>,
    /* onNavigateBack: () -> Unit */ // Commented out as it's not directly used here
) {
    // Debug logging
    Log.d("AnalyticsScreen", "UserStats received: questionsAnswered=${userStats.questionsAnswered}, correctAnswers=${userStats.correctAnswers}, streak=${userStats.streak}")
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

        OverallAccuracyChart(userStats = userStats)
        SubjectWiseAccuracyChart(userStats = userStats)
        TopicDrilldownChart(userStats = userStats)

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

        // Removed Comparative Analytics section as per request
        // Text("Comparative Analytics", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        // DifficultyBreakdownChart(userStats = userStats)
        // SubjectDifficultyMatrixChart(userStats = userStats)

        Spacer(modifier = Modifier.height(16.dp)) // Add some spacing at the end
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    MockMateTheme {
        val sampleUserStats = UserStats(
            questionsAnswered = 100,
            correctAnswers = 75,
            streak = 5,
            lastPracticeDate = Date(),
            subjectPerformance = mapOf(
                "Math" to SubjectPerformance("Math", 50, 30, 60f),
                "Science" to SubjectPerformance("Science", 30, 25, 83.33f),
                "History" to SubjectPerformance("History", 20, 10, 50f)
            )
        )
        val sampleTestAttempts = listOf(
            TestAttempt(id = "1", testId = "t1", startTime = Date(System.currentTimeMillis() - 86400000L * 2), score = 70f, isCompleted = true),
            TestAttempt(id = "2", testId = "t2", startTime = Date(System.currentTimeMillis() - 86400000L * 1), score = 85f, isCompleted = true),
            TestAttempt(id = "3", testId = "t3", startTime = Date(), score = 90f, isCompleted = true)
        )
        AnalyticsScreen(
            userStats = sampleUserStats,
            testAttempts = sampleTestAttempts,
            // onNavigateBack = {} // Commented out for preview as well
        )
    }
}
