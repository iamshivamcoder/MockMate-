package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics // Added for Analytics button
import androidx.compose.material.icons.filled.DataExploration
// import androidx.compose.material.icons.filled.Pets // Import for placeholder icon - No longer needed here
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
// import androidx.compose.material3.TextButton // No longer needed here
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.text.style.TextAlign // No longer needed here
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.UserStats
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.WelcomeCard
import com.example.mockmate.ui.components.ActionButton
import com.example.mockmate.ui.components.StreakInfoDialog // Import the extracted dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onPracticeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAnalyticsClick: () -> Unit, // Added for Analytics navigation
    repository: TestRepository // Removed default value
) {
    val userStats by repository.userStats.collectAsState(initial = UserStats(questionsAnswered = 0, correctAnswers = 0, currentStreak = 0, longestStreak = 0))
    var showStreakInfoDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MockMateTopBar(
                title = "MockMate",
                showBackButton = false,
                showSettings = false,
                currentStreak = userStats.currentStreak,
                onSettingsClick = onSettingsClick,
                onStreakClick = { showStreakInfoDialog = true },
                onImportClick = onImportClick, // Pass the onImportClick lambda - forcing recompile
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply innerPadding from Scaffold
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp), // Original padding for content
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WelcomeCard(userName = "")

            PracticeLureSection()

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "Start Practicing",
                icon = Icons.Default.PlayArrow,
                onClick = onPracticeClick,
                primaryColor = MaterialTheme.colorScheme.primary
            )

            ActionButton(
                text = "View History", // Changed text to be more specific
                icon = Icons.Default.DataExploration,
                onClick = onHistoryClick,
                primaryColor = MaterialTheme.colorScheme.secondary
            )

            ActionButton(
                text = "View Analytics", // New button for Analytics
                icon = Icons.Default.Analytics, // Using a new icon
                onClick = onAnalyticsClick,
                primaryColor = MaterialTheme.colorScheme.tertiary // Example color, adjust as needed
            )

            ActionButton(
                text = "Import Data",
                icon = Icons.Default.Upload,
                onClick = onImportClick,
                primaryColor = MaterialTheme.colorScheme.tertiary // Consider a different color if needed
            )
        }
    }

    if (showStreakInfoDialog) {
        StreakInfoDialog(onDismiss = { showStreakInfoDialog = false })
    }
}

@Composable
fun PracticeLureSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Thoda practice ho jaye?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Apne knowledge ko next level pe le jao!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = "Practice Rocket",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
