package com.example.mockmate.ui.components

import androidx.compose.runtime.Composable
import com.example.mockmate.model.UserStats

@Composable
fun AvgTimeSpentChart(/* userStats: UserStats */) { // Needs data on time spent vs recommended
    ChartPlaceholder(
        title = "Avg Time Spent vs Recommended Time (Grouped Bar Chart)",
        description = "Helps see whether the user is over/under spending time on each subject/topic."
    )
}

@Composable
fun PerQuestionAnalysisChart(/* userStats: UserStats */) { // Needs per question attempt data
    ChartPlaceholder(
        title = "Per Question Analysis (Box Plot / Scatter Plot)",
        description = "Time spent vs correctness (are they rushing and making mistakes?)."
    )
}

@Composable
fun StreakTrackerChart(userStats: UserStats) {
    ChartPlaceholder(
        title = "Streak Tracker (Flame/Fire Progress Bar)",
        description = "Current streak shown with visual fire icons 🔥."
    )
}

@Composable
fun TestAttemptsCounterChart(/* userStats: UserStats */) { // Needs count of test attempts
    ChartPlaceholder(
        title = "Test Attempts Counter (Badge/Level System)",
        description = "Show milestones → 5 tests, 10 tests, 20 tests = badges/unlocks."
    )
}

@Composable
fun DifficultyBreakdownChart(/* userStats: UserStats */) { // Needs accuracy broken down by difficulty
    ChartPlaceholder(
        title = "Difficulty Breakdown (Stacked Bars)",
        description = "Accuracy across Easy / Medium / Hard."
    )
}

@Composable
fun SubjectDifficultyMatrixChart(/* userStats: UserStats */) { // Needs accuracy per subject per difficulty
    ChartPlaceholder(
        title = "Subject vs Difficulty Matrix (Heatmap)",
        description = "Rows = subjects, Columns = difficulty, Cells = accuracy %."
    )
}