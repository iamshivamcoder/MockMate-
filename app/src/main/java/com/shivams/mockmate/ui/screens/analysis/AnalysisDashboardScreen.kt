package com.shivams.mockmate.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shivams.mockmate.model.analysis.AnalysisReport
import com.shivams.mockmate.ui.components.analysis.MentorFeedbackCard
import com.shivams.mockmate.ui.components.analysis.QuestionInsightItem
import com.shivams.mockmate.ui.components.analysis.ScoreHeader
import com.shivams.mockmate.ui.components.analysis.SummaryGrid
import com.shivams.mockmate.ui.viewmodels.AnalysisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Analysis Dashboard Screen - displays cognitive diagnosis results.
 * Hybrid layout: 2x2 summary grid at top + vertical list of question details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisDashboardScreen(
    onNavigateBack: () -> Unit,
    onNewAnalysis: () -> Unit,
    onViewHistory: () -> Unit = {},
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Try to restore cached report if we don't have one
    LaunchedEffect(Unit) {
        if (uiState.report == null) {
            viewModel.restoreLastReport()
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Analysis Results",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onViewHistory) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "View History"
                        )
                    }
                    IconButton(onClick = {
                        viewModel.clearAnalysis()
                        onNewAnalysis()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "New Analysis"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.report != null -> {
                DashboardContent(
                    report = uiState.report!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                // No report available
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No analysis available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upload a PDF to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    report: AnalysisReport,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Header - Most important at top
        item {
            ScoreHeader(
                score = report.score,
                accuracy = report.accuracy,
                totalQuestions = report.totalQuestions,
                correctCount = report.summary.correctCount
            )
        }
        
        // Header section with subject and date
        item {
            HeaderSection(report = report)
        }
        
        // 2x2 Summary Grid
        item {
            SummaryGrid(
                conceptCollapseCount = report.summary.conceptCollapseCount,
                flukeCount = report.summary.flukeCount,
                solidCount = report.summary.solidCount,
                doubtCount = report.summary.doubtCount
            )
        }
        
        // Accuracy overview
        item {
            AccuracyCard(report = report)
        }
        
        // Mentor Feedback Card (between score and breakdown)
        item {
            MentorFeedbackCard(feedback = report.mentorFeedback)
        }
        
        // Detailed breakdown header
        item {
            Text(
                text = "Detailed Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Question insights list
        items(
            items = report.questions.sortedBy { 
                // Sort by priority: Concept Collapse first, then Fluke, etc.
                when (it.cognitiveTag) {
                    com.shivams.mockmate.model.analysis.CognitiveTag.CONCEPT_COLLAPSE -> 0
                    com.shivams.mockmate.model.analysis.CognitiveTag.FLUKE -> 1
                    com.shivams.mockmate.model.analysis.CognitiveTag.DOUBT -> 2
                    com.shivams.mockmate.model.analysis.CognitiveTag.INTUITION -> 3
                    com.shivams.mockmate.model.analysis.CognitiveTag.SOLID -> 4
                }
            },
            key = { it.questionNumber }
        ) { question ->
            QuestionInsightItem(questionAnalysis = question)
        }
        
        // Bottom spacer
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderSection(report: AnalysisReport) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Subject title
        Text(
            text = report.testSubject,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Metadata row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatDate(report.analysisTimestamp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "•",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${report.totalQuestions} Questions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccuracyCard(report: AnalysisReport) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Overall Accuracy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(report.summary.overallAccuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Intuition Success",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(report.summary.intuitionAccuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
