package com.shivams.mockmate.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.ui.theme.MockMateTheme

@Composable
fun FinishTestDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    attemptedQuestions: Int,
    totalQuestions: Int,
    icon: ImageVector? = null // Added icon parameter
) {
    AlertDialog(
        shape = RoundedCornerShape(16.dp),
        icon = icon?.let { // Conditionally display icon
            { Icon(it, contentDescription = "Dialog Icon") }
        },
        onDismissRequest = onDismiss,
        title = { Text("Finish Test?", style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = { Text("You have attempted $attemptedQuestions out of $totalQuestions questions. Are you sure you want to finish?", style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Finish", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FinishTestDialogPreview() {
    MockMateTheme {
        FinishTestDialog(onDismiss = {}, onConfirm = {}, attemptedQuestions = 5, totalQuestions = 10)
    }
}

@Composable
fun ErrorAlertDialog(
    onDismiss: () -> Unit,
    errorMessage: String,
    title: String = "Error",
    icon: ImageVector = Icons.Filled.Warning
) {
    AlertDialog(
        shape = RoundedCornerShape(16.dp),
        icon = { Icon(icon, contentDescription = "Error Icon", tint = MaterialTheme.colorScheme.error) },
        title = { Text(text = "⚠️ " + title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = { Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium, maxLines = 5, overflow = TextOverflow.Ellipsis) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@Composable
fun StreakInfoDialog(
    onDismiss: () -> Unit,
    icon: ImageVector = Icons.Filled.Info // Added icon parameter
) {
    AlertDialog(
        shape = RoundedCornerShape(16.dp),
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = "Streak Info", tint = MaterialTheme.colorScheme.primary) }, // Use the icon parameter
        title = { Text("🔥 Daily Streaks", style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Text(
                "Maintain your daily streak by completing at least one question each day. " +
                        "Streaks help you stay motivated and build a consistent learning habit!",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@Composable
fun StreakProgressDialog(
    onDismiss: () -> Unit,
    currentStreak: Int,
    longestStreak: Int
) {
    AlertDialog(
        shape = RoundedCornerShape(16.dp),
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Info, contentDescription = "Streak Progress", tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Your Streak Progress", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text("Current Streak: $currentStreak days")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Longest Streak: $longestStreak days")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Keep up the great work!", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Awesome!", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StreakProgressDialogPreview() {
    MockMateTheme {
        StreakProgressDialog(onDismiss = {}, currentStreak = 12, longestStreak = 50)
    }
}
