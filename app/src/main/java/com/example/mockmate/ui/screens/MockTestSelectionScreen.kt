package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.TestCard

@Composable
fun MockTestSelectionScreen(
    onNavigateBack: () -> Unit,
    onTestSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    repository: TestRepository = com.example.mockmate.MockMateApplication.getTestRepository()
) {
    val allTests by repository.mockTests.collectAsState(initial = listOf())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val filteredTests = when (selectedTabIndex) {
        0 -> allTests.filter { it.difficulty == TestDifficulty.EASY }
        1 -> allTests.filter { it.difficulty == TestDifficulty.MEDIUM }
        2 -> allTests.filter { it.difficulty == TestDifficulty.HARD }
        else -> allTests
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MockMateTopBar(
            title = "Mock Prelims Tests",
            showBackButton = true,
            onBackClick = onNavigateBack,
            showSettings = true,
            onSettingsClick = onSettingsClick
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Test Difficulty",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Basic (30 min)") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Standard (60 min)") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Full Length (90 min)") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredTests) { test ->
                    TestCard(
                        test = test,
                        onClick = { onTestSelected(test.id) }
                    )
                }
            }
        }
    }
}