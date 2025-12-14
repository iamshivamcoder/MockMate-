package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.TestCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTheColumnSelectionScreen(
    onNavigateBack: () -> Unit,
    onTestSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    repository: TestRepository
) {
    val allMockTests by repository.getTestsByDifficulty(com.shivams.mockmate.model.TestDifficulty.EASY)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            MockMateTopBar(
                title = "Match The Column",
                showBackButton = true,
                onBackClick = onNavigateBack,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select a test to begin",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(allMockTests) { test ->
                    TestCard(
                        test = test,
                        onClick = { onTestSelected(test.id) },
                        onLongClick = { /* Handle long click */ }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
