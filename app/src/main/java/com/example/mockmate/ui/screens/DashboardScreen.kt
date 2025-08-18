package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.UserStats
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.UserStatsSection
import com.example.mockmate.ui.components.WelcomeCard
import com.example.mockmate.ui.components.ActionButton

@Composable
fun DashboardScreen(
    onPracticeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    repository: TestRepository = com.example.mockmate.MockMateApplication.getTestRepository()
) {
    val userStats by repository.userStats.collectAsState(initial = UserStats(questionsAnswered = 0, correctAnswers = 0, streak = 0))

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MockMateTopBar(
            title = "MockMate",
            showBackButton = false,
            showSettings = true,
            onSettingsClick = onSettingsClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Added for consistent spacing
        ) {
            WelcomeCard(userName = "") // Remove 'Aspirant' to avoid showing it in greeting

            Spacer(modifier = Modifier.height(16.dp)) // Adjusted from 32.dp

            UserStatsSection(userStats)

            Spacer(modifier = Modifier.height(24.dp)) // Adjusted from 40.dp

            ActionButton(
                text = "Start Practicing",
                icon = Icons.Default.PlayArrow,
                onClick = onPracticeClick,
                primaryColor = MaterialTheme.colorScheme.primary
            )

            ActionButton(
                text = "View History & Analytics",
                icon = Icons.Default.DataExploration,
                onClick = onHistoryClick,
                primaryColor = MaterialTheme.colorScheme.secondary
            )

            ActionButton(
                text = "Import Data",
                icon = Icons.Default.Upload,
                onClick = onImportClick,
                primaryColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
