package com.example.mockmate.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FinishTestDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    attemptedQuestions: Int,
    totalQuestions: Int
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish Test?") },
        text = {
            Column {
                Text("You have answered $attemptedQuestions out of $totalQuestions questions.")

                if (attemptedQuestions < totalQuestions) {
                    Text(
                        text = "There are ${totalQuestions - attemptedQuestions} unanswered questions.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    text = "Are you sure you want to finish this test?",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Yes, Finish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Test")
            }
        }
    )
}

@Preview
@Composable
fun FinishTestDialogPreview() {
    FinishTestDialog(
        onDismiss = {},
        onConfirm = {},
        attemptedQuestions = 7,
        totalQuestions = 10
    )
}

@Composable
fun ErrorAlertDialog(
    onDismiss: () -> Unit,
    errorMessage: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(errorMessage) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview
@Composable
fun ErrorAlertDialogPreview() {
    ErrorAlertDialog(
        onDismiss = {},
        errorMessage = "Something went terribly wrong!"
    )
}

@Composable
fun BatteryOptimizationDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Battery Optimization Warning") },
        text = { Text("To ensure smooth operation, please disable battery optimization for this app in your device settings. Otherwise, the app may be killed in the background and you may experience stuck loading screens or lost progress.") },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Preview
@Composable
fun BatteryOptimizationDialogPreview() {
    BatteryOptimizationDialog(onDismiss = {})
}
