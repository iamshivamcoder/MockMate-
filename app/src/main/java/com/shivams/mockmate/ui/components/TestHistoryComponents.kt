package com.shivams.mockmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.AttemptWithTest
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.screens.TestHistorySortCriteria
import com.shivams.mockmate.util.DateFormatUtils
import com.shivams.mockmate.util.sortTestHistoryAttempts
import java.util.Locale

@Composable
fun TestHistoryContent(
    modifier: Modifier = Modifier,
    userStats: UserStats,
    testAttempts: List<AttemptWithTest>,
    onViewResult: (String, String) -> Unit,
    onRenameClick: (AttemptWithTest) -> Unit,
    onDeleteClick: (AttemptWithTest) -> Unit
) {
    var sortBy by remember { mutableStateOf(TestHistorySortCriteria.DATE) }
    var sortAscending by remember { mutableStateOf(false) }

    val sortedAttempts = remember(testAttempts, sortBy, sortAscending) {
        sortTestHistoryAttempts(testAttempts, sortBy, sortAscending)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            UserStatsSection(userStats)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SectionHeader(text = "Test History")
        }

        item {
            SortControls(
                currentSortCriteria = sortBy,
                sortCriteriaOptions = TestHistorySortCriteria.entries.toList(),
                onSortCriteriaChange = { sortBy = it },
                sortAscending = sortAscending,
                onSortAscendingChange = { sortAscending = it },
                getDisplayName = { getSortCriteriaDisplayName(it) },
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        if (sortedAttempts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You haven\'t taken any tests yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            items(sortedAttempts, key = { it.attemptId }) { attempt ->
                TestHistoryItem(
                    attempt = attempt,
                    onViewResult = { onViewResult(attempt.attemptId, attempt.testId) },
                    onRenameClick = { onRenameClick(attempt) },
                    onDeleteClick = { onDeleteClick(attempt) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TestHistoryItem(
    attempt: AttemptWithTest,
    onViewResult: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Score-based accent color
    val scorePercentage = if (attempt.totalQuestions > 0) 
        attempt.score.toFloat() / attempt.totalQuestions * 100 else 0f
    val accentColor = when {
        scorePercentage >= 70 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        scorePercentage >= 40 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        else -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewResult() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Score indicator circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = accentColor.copy(alpha = 0.15f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${scorePercentage.toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = attempt.testName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = DateFormatUtils.formatTestHistoryDate(attempt.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Result") },
                            leadingIcon = {
                                Icon(Icons.Default.Assessment, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onViewResult()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onRenameClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Score details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score: ${attempt.score}/${attempt.totalQuestions}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = accentColor.copy(alpha = 0.1f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            scorePercentage >= 70 -> "Excellent"
                            scorePercentage >= 40 -> "Good"
                            else -> "Needs Work"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar with accent color
            val progress = if (attempt.totalQuestions > 0) attempt.score.toFloat() / attempt.totalQuestions else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .height(8.dp)
                        .background(
                            color = accentColor,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}


private fun getSortCriteriaDisplayName(criteria: TestHistorySortCriteria): String {
    return when (criteria) {
        TestHistorySortCriteria.DATE -> "Date"
        TestHistorySortCriteria.SCORE -> "Score"
        TestHistorySortCriteria.TEST_NAME -> "Test Name"
    }
}
