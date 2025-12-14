package com.shivams.mockmate.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

private const val EXCELLENT_THRESHOLD = 80
private const val GOOD_THRESHOLD = 60
private const val PROGRESS_THRESHOLD = 40

private val COLOR_EXCELLENT = Color(0xFF4CAF50) // Green
private val COLOR_GOOD = Color(0xFF2196F3) // Blue
private val COLOR_PROGRESS = Color(0xFFFFC107) // Yellow
private val COLOR_NEEDS_IMPROVEMENT = Color(0xFFF44336) // Red

private const val FEEDBACK_EXCELLENT = "Excellent! You\'ve mastered this material."
private const val FEEDBACK_GOOD = "Good job! You have a solid understanding of the material."
private const val FEEDBACK_PROGRESS = "You\'re making progress, but should review some concepts."
private const val FEEDBACK_NEEDS_IMPROVEMENT = "You need to focus on improving your understanding of this material."

@Composable
fun OverallScoreCard(
    score: Float,
    totalMarks: Float,
    scorePercentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Score",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${String.format(Locale.getDefault(), "%.1f", score)}/${totalMarks.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${String.format(Locale.getDefault(), "%.1f", scorePercentage)}%",
                style = MaterialTheme.typography.titleLarge,
                color = getScoreColor(scorePercentage)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                progress = { scorePercentage / 100f },
                color = getScoreColor(scorePercentage),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getScoreFeedback(scorePercentage),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SubjectPerformanceCard(
    subject: String,
    accuracy: Float,
    attempted: Int,
    correct: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Accuracy: ${(accuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        progress = { accuracy },
                        color = getScoreColor(accuracy * 100),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$correct/$attempted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "questions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Returns a color based on the score percentage.
 */
fun getScoreColor(percentage: Float): Color {
    return when {
        percentage >= EXCELLENT_THRESHOLD -> COLOR_EXCELLENT
        percentage >= GOOD_THRESHOLD -> COLOR_GOOD
        percentage >= PROGRESS_THRESHOLD -> COLOR_PROGRESS
        else -> COLOR_NEEDS_IMPROVEMENT
    }
}

/**
 * Returns feedback text based on the score percentage.
 */
fun getScoreFeedback(percentage: Float): String {
    return when {
        percentage >= EXCELLENT_THRESHOLD -> FEEDBACK_EXCELLENT
        percentage >= GOOD_THRESHOLD -> FEEDBACK_GOOD
        percentage >= PROGRESS_THRESHOLD -> FEEDBACK_PROGRESS
        else -> FEEDBACK_NEEDS_IMPROVEMENT
    }
}
