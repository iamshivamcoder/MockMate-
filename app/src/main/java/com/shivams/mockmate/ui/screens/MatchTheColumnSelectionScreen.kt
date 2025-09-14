package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.QuestionType
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.TestCard
import java.util.Locale

// Enum for sorting criteria - reused from MockTestSelectionScreen
// enum class SortCriteria { NONE, MARKS, DATE, LAST_GIVEN, FORGETTING_CURVE }

// Wrapper data class for display and sorting - reused from MockTestSelectionScreen
// data class DisplayableMockTest( ... )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTheColumnSelectionScreen(
    onNavigateBack: () -> Unit,
    onTestSelected: (String) -> Unit, // Navigates to MatchTheColumnScreen (test taking)
    onSettingsClick: () -> Unit,
    repository: TestRepository = com.shivams.mockmate.MockMateApplication.getTestRepository()
) {
    // Fetch raw MockTest objects
    val allRawMockTests by repository.mockTests.collectAsState(initial = emptyList())

    // Filter for Match the Column tests
    val matchTheColumnRawTests = remember(allRawMockTests) {
        allRawMockTests.filter { mockTest ->
            // Assuming a test is "Match the Column" if its first question is of that type
            mockTest.questions.firstOrNull()?.type == QuestionType.MATCH_THE_COLUMN
        }
    }

    // Map filtered raw MockTests to DisplayableMockTests
    val allDisplayableTests = remember(matchTheColumnRawTests) {
        matchTheColumnRawTests.map {
            mockTest ->
            DisplayableMockTest(
                mockTest = mockTest,
                latestScore = null, // Placeholder: To be fetched from TestAttempt via TestRepository
                lastAttemptDate = null // Placeholder: To be fetched from TestAttempt via TestRepository
            )
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var currentSortCriteria by remember { mutableStateOf(SortCriteria.DATE) } // Default sort criteria
    var sortAscending by remember { mutableStateOf(false) } // Default to descending
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    val filteredAndSortedTests = remember(allDisplayableTests, selectedTabIndex, currentSortCriteria, sortAscending) {
        val difficultyFiltered = when (selectedTabIndex) {
            0 -> allDisplayableTests.filter { it.mockTest.difficulty == TestDifficulty.EASY }
            1 -> allDisplayableTests.filter { it.mockTest.difficulty == TestDifficulty.MEDIUM }
            2 -> allDisplayableTests.filter { it.mockTest.difficulty == TestDifficulty.HARD }
            else -> allDisplayableTests
        }

        when (currentSortCriteria) {
            SortCriteria.MARKS -> if (sortAscending) difficultyFiltered.sortedBy { it.latestScore } else difficultyFiltered.sortedByDescending { it.latestScore }
            SortCriteria.DATE -> if (sortAscending) difficultyFiltered.sortedBy { it.mockTest.creationDate } else difficultyFiltered.sortedByDescending { it.mockTest.creationDate }
            SortCriteria.LAST_GIVEN -> if (sortAscending) difficultyFiltered.sortedBy { it.lastAttemptDate } else difficultyFiltered.sortedByDescending { it.lastAttemptDate }
            SortCriteria.FORGETTING_CURVE -> {
                // Placeholder for forgetting curve logic
                if (sortAscending) difficultyFiltered.sortedBy { it.mockTest.creationDate } // Fallback
                else difficultyFiltered.sortedByDescending { it.mockTest.creationDate } // Fallback
            }
            SortCriteria.NONE -> difficultyFiltered
        }
    }

    fun getSortCriteriaDisplayName(criteria: SortCriteria): String {
        return criteria.name.replace('_', ' ').lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MockMateTopBar(
            title = "Match The Column Tests",
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
                    text = { Text("Basic (30 min)") } // Assuming these durations are still relevant
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

            Spacer(modifier = Modifier.height(8.dp))

            // Sort Options Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort Options Icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = sortDropdownExpanded,
                    onExpandedChange = { sortDropdownExpanded = !sortDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = getSortCriteriaDisplayName(currentSortCriteria),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sort by") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sortDropdownExpanded,
                        onDismissRequest = { sortDropdownExpanded = false }
                    ) {
                        SortCriteria.entries.filterNot { it == SortCriteria.NONE }.forEach { selectionCriteria ->
                            DropdownMenuItem(
                                text = { Text(getSortCriteriaDisplayName(selectionCriteria)) },
                                onClick = {
                                    currentSortCriteria = selectionCriteria
                                    sortDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { sortAscending = !sortAscending }) {
                    Icon(
                        imageVector = if (sortAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = if (sortAscending) "Sort Ascending" else "Sort Descending"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredAndSortedTests) { displayableTest ->
                    TestCard(
                        test = displayableTest.mockTest, // Pass the original MockTest to TestCard
                        onClick = { onTestSelected(displayableTest.mockTest.id) }
                    )
                }
            }
        }
    }
}
