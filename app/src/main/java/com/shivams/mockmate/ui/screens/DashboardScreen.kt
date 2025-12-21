package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.components.ActionButton
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.MotivationCard
import com.shivams.mockmate.ui.components.PrelimsCountdownCard
import com.shivams.mockmate.ui.components.WelcomeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onPracticeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onStreakClick: () -> Unit,
    onSavedQuestionsClick: () -> Unit,
    onMentorChatClick: () -> Unit,
    repository: TestRepository
) {
    val userStats by repository.userStats.collectAsState(initial = UserStats(questionsAnswered = 0, correctAnswers = 0, currentStreak = 0, longestStreak = 0))
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
                onStreakClick = onStreakClick,
                onImportClick = onImportClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Welcome Card with gradient
            WelcomeCard(userName = "")
            
            // Motivation Card with regional phrases
            MotivationCard()
            
            // Prelims Countdown Card
            PrelimsCountdownCard()

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                text = "Start Practicing",
                icon = Icons.Default.PlayArrow,
                onClick = onPracticeClick,
                primaryColor = MaterialTheme.colorScheme.primary
            )

            ActionButton(
                text = "View History",
                icon = Icons.Default.DataExploration,
                onClick = onHistoryClick,
                primaryColor = MaterialTheme.colorScheme.secondary
            )

            ActionButton(
                text = "View Analytics",
                icon = Icons.Default.Analytics,
                onClick = onAnalyticsClick,
                primaryColor = MaterialTheme.colorScheme.tertiary
            )

            ActionButton(
                text = "Import Data",
                icon = Icons.Default.Upload,
                onClick = onImportClick,
                primaryColor = MaterialTheme.colorScheme.tertiary
            )

            ActionButton(
                text = "Saved Questions",
                icon = Icons.Default.Bookmark,
                onClick = onSavedQuestionsClick,
                primaryColor = MaterialTheme.colorScheme.secondary
            )

            ActionButton(
                text = "Ask AI Mentor",
                icon = Icons.Default.School,
                onClick = onMentorChatClick,
                primaryColor = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
