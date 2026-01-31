package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.PracticeMode
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.MotivationalQuoteCard
import com.shivams.mockmate.ui.components.PracticeModeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeModeSelectionScreen(
    onNavigateBack: () -> Unit,
    onMockTestClick: () -> Unit,
    onParagraphAnalysisClick: () -> Unit = {},
    onTrueFalseClick: () -> Unit = {},
    onSettingsClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MockMateTopBar(
                title = "Practice Modes",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showSettings = true,
                onSettingsClick = onSettingsClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MotivationalQuoteCard()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose a Practice Mode",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            PracticeModeCard(
                mode = PracticeMode.MOCK_TEST,
                title = "Mock Prelims",
                description = "Take a full-length mock test to simulate the UPSC Preliminary exam",
                onClick = onMockTestClick
            )

            PracticeModeCard(
                mode = PracticeMode.PARAGRAPH_ANALYSIS,
                title = "Match the Column",
                description = "Test your knowledge by matching items from two columns.",
                onClick = onParagraphAnalysisClick
            )
            
            PracticeModeCard(
                mode = PracticeMode.TRUE_FALSE_APTITUDE,
                title = "True or False",
                description = "Swipe-based training to detect hidden qualifiers and half-truths in UPSC-style statements.",
                onClick = onTrueFalseClick
            )
        }
    }
}