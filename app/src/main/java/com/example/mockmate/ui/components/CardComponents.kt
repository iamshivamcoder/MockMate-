package com.example.mockmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.PracticeMode
import com.example.mockmate.ui.theme.extendedColorScheme

/**
 * A card component to display a statistic with a title and value.
 *
 * @param title The title of the statistic.
 * @param value The value of the statistic.
 * @param modifier Optional [Modifier] for this component.
 */
@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * A card component to represent a practice mode.
 *
 * @param mode The [PracticeMode] to display.
 * @param title The title of the practice mode.
 * @param description A short description of the practice mode.
 * @param onClick Lambda to be invoked when the card is clicked.
 * @param modifier Optional [Modifier] for this component.
 */
@Composable
fun PracticeModeCard(
    mode: PracticeMode,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconColor = when (mode) {
                PracticeMode.DAILY_CHALLENGE -> MaterialTheme.colorScheme.tertiary
                PracticeMode.FOCUSED_PRACTICE -> MaterialTheme.colorScheme.primary
                PracticeMode.CUSTOM_PRACTICE -> MaterialTheme.colorScheme.secondary
                PracticeMode.MOCK_TEST -> MaterialTheme.extendedColorScheme.mockTestColor
                PracticeMode.PARAGRAPH_ANALYSIS -> MaterialTheme.extendedColorScheme.paragraphAnalysisColor
            }
            
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f))
                    .border(2.dp, iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (mode) {
                        PracticeMode.DAILY_CHALLENGE -> "DC"
                        PracticeMode.FOCUSED_PRACTICE -> "FP"
                        PracticeMode.CUSTOM_PRACTICE -> "CP"
                        PracticeMode.MOCK_TEST -> "MT"
                        PracticeMode.PARAGRAPH_ANALYSIS -> "PA"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to $title",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * A card component to display information about a mock test.
 *
 * @param test The [MockTest] data to display.
 * @param onClick Lambda to be invoked when the card is clicked.
 * @param modifier Optional [Modifier] for this component.
 */
@Composable
fun TestCard(
    test: MockTest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = test.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${test.questions.size} Questions",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${test.timeLimit} Minutes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                DifficultyBadge(difficulty = test.difficulty) // DifficultyBadge is in BadgeComponents.kt
            }
        }
    }
}
