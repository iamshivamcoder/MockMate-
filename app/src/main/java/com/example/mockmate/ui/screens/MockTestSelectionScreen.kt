package com.example.mockmate.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.MockTest
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.ui.components.MockMateTopBar
import com.example.mockmate.ui.components.TestCard
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

// Enum for sorting criteria
enum class SortCriteria {
    NONE,
    MARKS,
    DATE, // This will sort by MockTest.creationDate
    LAST_GIVEN,
    FORGETTING_CURVE // Spaced Repetition
}

// Wrapper data class for display and sorting
data class DisplayableMockTest(
    val mockTest: MockTest,
    val latestScore: Float? = null,
    val lastAttemptDate: Date? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MockTestSelectionScreen(
    onNavigateBack: () -> Unit,
    onTestSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    repository: TestRepository = com.example.mockmate.MockMateApplication.getTestRepository()
) {
    // Fetch raw MockTest objects
    val allRawMockTests by repository.mockTests.collectAsState(initial = emptyList())
    // Fetch all TestAttempt objects
    val allTestAttempts by repository.getAllTestAttempts().collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var testToDelete by remember { mutableStateOf<MockTest?>(null) }

    // Map raw MockTests to DisplayableMockTests, now including attempt data
    val allDisplayableTests = remember(allRawMockTests, allTestAttempts) {
        allRawMockTests.map { mockTest ->
            val relevantAttempts = allTestAttempts
                .filter { it.testId == mockTest.id && it.isCompleted }
                .sortedByDescending { it.endTime } // Get the most recent completed attempt first

            DisplayableMockTest(
                mockTest = mockTest,
                latestScore = relevantAttempts.firstOrNull()?.score,
                lastAttemptDate = relevantAttempts.firstOrNull()?.endTime
            )
        }
    }

    val tabTitles = listOf("Basic (30 min)", "Standard (60 min)", "Full Length (90 min)")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    var currentSortCriteria by remember { mutableStateOf(SortCriteria.DATE) } // Default sort criteria
    var sortAscending by remember { mutableStateOf(false) } // Default to descending
    var sortDropdownExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun getSortCriteriaDisplayName(criteria: SortCriteria): String {
        return criteria.name.replace('_', ' ').lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
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

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Test") },
                text = { Text("Are you sure you want to delete \"${testToDelete?.name}\"? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            testToDelete?.let {
                                // Call repository delete function here
                                // repository.deleteMockTest(it.id) // Placeholder
                                println("Deleting test: ${it.name} (ID: ${it.id})") // Actual deletion logic pending
                                scope.launch { // Ensure deletion happens in a coroutine if repository function is suspend
                                    repository.deleteMockTestById(it.id) // Assumed suspend fun in repo
                                }
                            }
                            showDeleteDialog = false
                            testToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showDeleteDialog = false
                        testToDelete = null 
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

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

            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) { page -> // page index
                val difficultyFiltered = remember(allDisplayableTests, page) {
                    when (page) {
                        0 -> allDisplayableTests.filter { it.mockTest.difficulty == TestDifficulty.EASY }
                        1 -> allDisplayableTests.filter { it.mockTest.difficulty == TestDifficulty.MEDIUM }
                        2 -> allDisplayableTests.filter { it.mockTest.difficulty == TestDifficulty.HARD }
                        else -> allDisplayableTests
                    }
                }

                val filteredAndSortedTests = remember(difficultyFiltered, currentSortCriteria, sortAscending) {
                    when (currentSortCriteria) {
                        SortCriteria.MARKS -> {
                            if (sortAscending) difficultyFiltered.sortedBy { it.latestScore }
                            else difficultyFiltered.sortedByDescending { it.latestScore }
                        }
                        SortCriteria.DATE -> {
                            if (sortAscending) difficultyFiltered.sortedBy { it.mockTest.creationDate }
                            else difficultyFiltered.sortedByDescending { it.mockTest.creationDate }
                        }
                        SortCriteria.LAST_GIVEN -> {
                            if (sortAscending) difficultyFiltered.sortedWith(compareBy(nullsLast()) { it.lastAttemptDate })
                            else difficultyFiltered.sortedWith(compareByDescending(nullsFirst()) { it.lastAttemptDate })
                        }
                        SortCriteria.FORGETTING_CURVE -> {
                            // Placeholder for forgetting curve logic - current fallback to DATE
                            if (sortAscending) difficultyFiltered.sortedBy { it.mockTest.creationDate } 
                            else difficultyFiltered.sortedByDescending { it.mockTest.creationDate }
                        }
                        SortCriteria.NONE -> difficultyFiltered
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredAndSortedTests) { displayableTest ->
                        TestCard(
                            test = displayableTest.mockTest,
                            onClick = { onTestSelected(displayableTest.mockTest.id) },
                            onLongClick = {
                                testToDelete = displayableTest.mockTest
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}
