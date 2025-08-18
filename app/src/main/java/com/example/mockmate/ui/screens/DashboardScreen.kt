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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.UserStats
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.ProgressBar
import com.example.mockmate.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeCard(userName = "") // Remove 'Aspirant' to avoid showing it in greeting

            Spacer(modifier = Modifier.height(32.dp))

            UserStatsSection(userStats)

            Spacer(modifier = Modifier.height(40.dp))

            ActionButton(
                text = "Start Practicing",
                icon = Icons.Default.PlayArrow,
                onClick = onPracticeClick,
                primaryColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionButton(
                text = "View History & Analytics",
                icon = Icons.Default.DataExploration,
                onClick = onHistoryClick,
                primaryColor = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionButton(
                text = "Import Data",
                icon = Icons.Default.Upload,
                onClick = onImportClick,
                primaryColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun UserStatsSection(userStats: UserStats) {
    SectionHeader(text = "Your Progress")

    val accuracy = if (userStats.questionsAnswered > 0) {
        userStats.correctAnswers.toFloat() / userStats.questionsAnswered
    } else {
        0f
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(
                title = "Questions Attempted",
                value = userStats.questionsAnswered.toString(),
                modifier = Modifier.weight(1f)
            )
            StatColumn(
                title = "Current Streak",
                value = "${userStats.streak} days",
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Overall Accuracy: ${(accuracy * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProgressBar(progress = accuracy)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Correct Answers: ${userStats.correctAnswers} out of ${userStats.questionsAnswered}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Focus on consistent daily practice to improve your UPSC preparation!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun WelcomeCard(userName: String) {
    val greeting = remember { getGreeting() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (userName.isNotEmpty()) "$greeting $userName!" else "$greeting!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ready to continue your UPSC preparation journey?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date()) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatColumn(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greetings = listOf(
        Pair(0..11, listOf(
            "Namaste, Early Bird",
            "Good Morning, Warrior",
            "Rise and Shine, Champion",
            "Suprabhat, Learner"
        )),
        Pair(12..15, listOf(
            "Namaste, Determined Soul",
            "Keep Going, Warrior",
            "Stay Strong, Fighter",
            "Power Through, Champion"
        )),
        Pair(16..19, listOf(
            "Good Evening, Achiever",
            "Pushing Forward, Warrior",
            "Stay Focused, Champion",
            "Almost There, Fighter"
        )),
        Pair(20..23, listOf(
            "Keep At It, Night Owl",
            "Burning Midnight Oil, Warrior",
            "Dedication Personified",
            "Night Mode: Activated"
        ))
    )

    val timeSlot = greetings.find { (range, _) -> hour in range }
    val greetingList = timeSlot?.second ?: greetings[0].second
    return greetingList.random()
}
