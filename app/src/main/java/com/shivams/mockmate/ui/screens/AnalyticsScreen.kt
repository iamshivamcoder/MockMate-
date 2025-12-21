package com.shivams.mockmate.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.components.analytics.AnimatedAccuracyRing
import com.shivams.mockmate.ui.components.analytics.EmptyAnalyticsState
import com.shivams.mockmate.ui.components.analytics.PerformanceTrendCard
import com.shivams.mockmate.ui.components.analytics.QuickStatsRow
import com.shivams.mockmate.ui.components.analytics.SubjectPerformanceCard
import com.shivams.mockmate.ui.components.analytics.WeeklyActivityCard
import com.shivams.mockmate.ui.theme.MockMateTheme

@Composable
fun AnalyticsScreen(
    userStats: UserStats,
    testAttempts: List<TestAttempt>,
    isLoading: Boolean
) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            showContent = true
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (userStats.questionsAnswered == 0 && testAttempts.isEmpty()) {
        EmptyAnalyticsState()
    } else {
        Log.d("AnalyticsScreen", "Rendering with ${testAttempts.size} attempts, ${userStats.questionsAnswered} questions answered")
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(300)) + slideInVertically(tween(400)) { -50 }
            ) {
                Text(
                    text = "Your Performance",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Animated accuracy ring
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 100)) + slideInVertically(tween(500, 100)) { -30 }
            ) {
                AnimatedAccuracyRing(
                    correctAnswers = userStats.correctAnswers,
                    totalQuestions = userStats.questionsAnswered
                )
            }
            
            // Quick stats row
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 200)) + slideInVertically(tween(500, 200)) { -30 }
            ) {
                QuickStatsRow(
                    testAttempts = testAttempts,
                    userStats = userStats
                )
            }
            
            // Performance chart
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 300)) + slideInVertically(tween(500, 300)) { -30 }
            ) {
                PerformanceTrendCard(testAttempts = testAttempts, userStats = userStats)
            }
            
            // Subject performance
            if (userStats.subjectPerformance.isNotEmpty()) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400, 400)) + slideInVertically(tween(500, 400)) { -30 }
                ) {
                    SubjectPerformanceCard(userStats = userStats)
                }
            }
            
            // Weekly activity
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 500)) + slideInVertically(tween(500, 500)) { -30 }
            ) {
                WeeklyActivityCard(testAttempts = testAttempts)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    MockMateTheme {
        AnalyticsScreen(
            userStats = UserStats(
                questionsAnswered = 50,
                correctAnswers = 35,
                currentStreak = 5
            ),
            testAttempts = emptyList(),
            isLoading = false
        )
    }
}
