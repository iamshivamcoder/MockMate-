package com.shivams.mockmate.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FlagCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.Question
import com.shivams.mockmate.model.QuestionStatus
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.ui.components.MockMateTopBar

/**
 * Data class to hold question with its attempt context for display
 */
data class SavedQuestion(
    val question: Question,
    val testName: String,
    val attemptDate: java.util.Date,
    val status: QuestionStatus,
    val userSelectedOption: Int?,
    val isCorrect: Boolean
)

/**
 * Screen displaying Flagged (Marked for Review) and Bookmarked questions
 * with a tab switcher between the two lists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlaggedBookmarkedScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Flagged", "Bookmarked")
    
    val testAttempts by repository.getAllTestAttempts().collectAsState(initial = emptyList())
    var isLoading by remember { mutableStateOf(true) }
    var flaggedQuestions by remember { mutableStateOf<List<SavedQuestion>>(emptyList()) }
    var bookmarkedQuestions by remember { mutableStateOf<List<SavedQuestion>>(emptyList()) }
    
    // Process attempts to extract flagged and bookmarked questions
    LaunchedEffect(testAttempts) {
        isLoading = true
        val flagged = mutableListOf<SavedQuestion>()
        val bookmarked = mutableListOf<SavedQuestion>()
        
        testAttempts.forEach { attempt ->
            val test = repository.getTestById(attempt.testId)
            if (test != null) {
                attempt.userAnswers.forEach { (questionId, userAnswer) ->
                    val question = test.questions.find { it.id == questionId }
                    if (question != null) {
                        val savedQuestion = SavedQuestion(
                            question = question,
                            testName = test.name,
                            attemptDate = attempt.startTime,
                            status = userAnswer.status,
                            userSelectedOption = userAnswer.selectedOptionIndex,
                            isCorrect = userAnswer.selectedOptionIndex == question.correctOptionIndex
                        )
                        
                        when (userAnswer.status) {
                            QuestionStatus.MARKED_FOR_REVIEW -> flagged.add(savedQuestion)
                            QuestionStatus.BOOKMARKED -> bookmarked.add(savedQuestion)
                            else -> { /* Ignore other statuses */ }
                        }
                    }
                }
            }
        }
        
        flaggedQuestions = flagged.sortedByDescending { it.attemptDate }
        bookmarkedQuestions = bookmarked.sortedByDescending { it.attemptDate }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            MockMateTopBar(
                title = "Saved Questions",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row - Custom styled tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    val tabColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        },
                        label = "tabColor"
                    )
                    
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (index == 0) {
                                    if (isSelected) Icons.Filled.Flag else Icons.Outlined.FlagCircle
                                } else {
                                    if (isSelected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = title,
                                tint = tabColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                color = tabColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            
                            // Count badge
                            val count = if (index == 0) flaggedQuestions.size else bookmarkedQuestions.size
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Content based on selected tab
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                selectedTabIndex == 0 -> {
                    // Flagged Questions
                    if (flaggedQuestions.isEmpty()) {
                        EmptyStateMessage(
                            icon = Icons.Filled.Flag,
                            title = "No Flagged Questions",
                            message = "Questions you mark for review during tests will appear here"
                        )
                    } else {
                        QuestionsList(questions = flaggedQuestions)
                    }
                }
                else -> {
                    // Bookmarked Questions
                    if (bookmarkedQuestions.isEmpty()) {
                        EmptyStateMessage(
                            icon = Icons.Filled.Bookmark,
                            title = "No Bookmarked Questions",
                            message = "Questions you bookmark during tests will appear here"
                        )
                    } else {
                        QuestionsList(questions = bookmarkedQuestions)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionsList(questions: List<SavedQuestion>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        items(questions) { savedQuestion ->
            SavedQuestionCard(savedQuestion = savedQuestion)
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SavedQuestionCard(
    savedQuestion: SavedQuestion,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with status icon and test name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (savedQuestion.status == QuestionStatus.MARKED_FOR_REVIEW) 
                            Icons.Filled.Flag else Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = if (savedQuestion.status == QuestionStatus.MARKED_FOR_REVIEW)
                            Color(0xFFFF6B35) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = savedQuestion.testName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Subject badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = savedQuestion.question.subject,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Question text
            Text(
                text = savedQuestion.question.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Expanded content with options and answer
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Options
                savedQuestion.question.options?.forEachIndexed { index, option ->
                    val isSelected = savedQuestion.userSelectedOption == index
                    val isCorrect = savedQuestion.question.correctOptionIndex == index
                    
                    val backgroundColor = when {
                        isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.15f)
                        else -> Color.Transparent
                    }
                    
                    val textColor = when {
                        isCorrect -> Color(0xFF2E7D32)
                        isSelected && !isCorrect -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(backgroundColor)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + index)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
                
                // Explanation
                if (savedQuestion.question.explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Explanation",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = savedQuestion.question.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
            
            // Tap to expand indicator
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to see answer",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
