package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.shivams.mockmate.data.repositories.SettingsRepository
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.AppSettings
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.SortControls
import com.shivams.mockmate.ui.components.TestCard
import com.shivams.mockmate.util.filterTests
import com.shivams.mockmate.util.sortTests
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
    onImportClick: () -> Unit,
    repository: TestRepository,
    settingsRepository: SettingsRepository // Added settings repo
) {
    // Fetch raw MockTest objects
    val allRawMockTests by repository.mockTests.collectAsState(initial = emptyList())
    // Fetch all TestAttempt objects
    val allTestAttempts by repository.getAllTestAttempts().collectAsState(initial = emptyList())
    // Fetch app settings
    val appSettings by settingsRepository.settings.collectAsState(initial = AppSettings()) // Added

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
            showSettings = false,
            onSettingsClick = onSettingsClick,
            onImportClick = onImportClick
        )

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Test") },
                text = { Text("Are you sure you want to delete ${testToDelete?.name}? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            testToDelete?.let {
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

            // Use the new SortControls Composable
            SortControls(
                currentSortCriteria = currentSortCriteria,
                sortCriteriaOptions = SortCriteria.entries.filterNot { it == SortCriteria.NONE },
                onSortCriteriaChange = { currentSortCriteria = it },
                sortAscending = sortAscending,
                onSortAscendingChange = { sortAscending = it },
                getDisplayName = ::getSortCriteriaDisplayName,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) { page -> // page index
                val difficultyFiltered = remember(allDisplayableTests, page) {
                    filterTests(allDisplayableTests, page) // Use utility function
                }

                val filteredAndSortedTests = remember(difficultyFiltered, currentSortCriteria, sortAscending) {
                    sortTests(difficultyFiltered, currentSortCriteria, sortAscending) // Use utility function
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredAndSortedTests, key = { it.mockTest.id }) { displayableTest -> // Added key for better performance
                        TestCard(
                            test = displayableTest.mockTest,
                            onClick = { onTestSelected(displayableTest.mockTest.id) },
                            onLongClick = {
                                testToDelete = displayableTest.mockTest
                                showDeleteDialog = true
                            },
                            pulsateBadges = appSettings.pulsatingBadgesEnabled // Fixed this line
                        )
                    }
                }
            }
        }
    }
}
