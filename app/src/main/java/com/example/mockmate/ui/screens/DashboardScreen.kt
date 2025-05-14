package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.UserStats
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.ProgressBar
import com.example.mockmate.ui.components.SectionHeader
import com.example.mockmate.ui.components.StatCard

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome to MockMate",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your UPSC preparation companion",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            UserStatsSection(userStats)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onPracticeClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Start Practicing")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onHistoryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "View History & Analytics")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Import Data")
            }
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
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Questions",
            value = userStats.questionsAnswered.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Daily Streak",
            value = "${userStats.streak} days",
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Accuracy: ${(accuracy * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ProgressBar(progress = accuracy)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Focus on consistent daily practice to improve your UPSC preparation!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}