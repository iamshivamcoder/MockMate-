package com.shivams.mockmate.ui.components

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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewResult() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = attempt.testName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 4.dp)
                )

                Text(
                    text = DateFormatUtils.formatTestHistoryDate(attempt.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${attempt.score}/${attempt.totalQuestions}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "(${(attempt.score.toFloat() / attempt.totalQuestions * 100).toInt()}%})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = if (attempt.totalQuestions > 0) attempt.score.toFloat() / attempt.totalQuestions else 0f
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(6.dp),
                progress = { progress }
            )
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
