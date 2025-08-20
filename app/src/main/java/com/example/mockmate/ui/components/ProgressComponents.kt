package com.example.mockmate.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A linear progress bar.
 *
 * @param progress The current progress, a float between 0.0 and 1.0.
 * @param modifier Optional [Modifier] for this component.
 */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        progress = { progress }, // Directly use lambda for progress
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    )
}

@Composable
fun TestProgressHeader(
    currentQuestion: Int,
    totalQuestions: Int,
    timeRemaining: Long
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question $currentQuestion of $totalQuestions",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formatTime(timeRemaining),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (timeRemaining < 60 && timeRemaining != 0L /* Allow 0 for end state */) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = { if (totalQuestions > 0) currentQuestion.toFloat() / totalQuestions else 0f },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Preview
@Composable
fun TestProgressHeaderPreview() {
    TestProgressHeader(
        currentQuestion = 5,
        totalQuestions = 10,
        timeRemaining = 300L
    )
}

// Utility function, assuming it was available to TestProgressHeader in its original context
// If not, it needs to be imported or defined here as well.
// For now, I'll assume it's globally accessible or should be moved here too.
fun formatTime(seconds: Long): String {
    if (seconds < 0) return "00:00" // Handle initial or invalid states
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
}
