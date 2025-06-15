package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme

@Composable
fun AIQuestionResultScreen(
    accuracy: Float,
    subjects: List<String>,
    onRegenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "AI Question Result", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Accuracy: ${accuracy * 100}%")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Subjects: ${subjects.joinToString(", ")}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRegenerateClick) {
            Text(text = "Want more like this?")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AIQuestionResultScreenPreview() {
    AIQuestionResultScreen(
        accuracy = 0.75f,
        subjects = listOf("Polity", "Economics"),
        onRegenerateClick = {}
    )
}