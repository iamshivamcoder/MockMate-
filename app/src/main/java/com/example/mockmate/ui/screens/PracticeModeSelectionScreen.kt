package com.example.mockmate.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mockmate.model.PracticeMode
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.MotivationalQuoteCard
import com.example.mockmate.ui.components.PracticeModeCard

@Composable
fun PracticeModeSelectionScreen(
    onNavigateBack: () -> Unit,
    onMockTestClick: () -> Unit,
    onParagraphAnalysisClick: () -> Unit = {},
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MockMateTopBar(
            title = "Practice Modes",
            showBackButton = true,
            onBackClick = onNavigateBack,
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
                title = "Paragraph Analysis",
                description = "Paste a paragraph you've read to test your understanding and knowledge",
                onClick = onParagraphAnalysisClick
            )
        }
    }
}