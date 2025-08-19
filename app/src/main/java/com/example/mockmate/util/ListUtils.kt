package com.example.mockmate.util

import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.ui.screens.DisplayableMockTest
import com.example.mockmate.ui.screens.SortCriteria
import com.example.mockmate.model.AttemptWithTest // Updated import
import com.example.mockmate.ui.screens.TestHistorySortCriteria // Added import

/**
 * Filters a list of DisplayableMockTest objects based on a page index representing difficulty.
 *
 * @param tests The list of DisplayableMockTest objects to filter.
 * @param pageIndex The page index (0 for Easy, 1 for Medium, 2 for Hard).
 * @return A new list containing only the tests that match the difficulty for the given page.
 */
fun filterTests( // Renamed from filterMockTestsByDifficultyPage
    tests: List<DisplayableMockTest>,
    pageIndex: Int
): List<DisplayableMockTest> {
    return when (pageIndex) {
        0 -> tests.filter { it.mockTest.difficulty == TestDifficulty.EASY }
        1 -> tests.filter { it.mockTest.difficulty == TestDifficulty.MEDIUM }
        2 -> tests.filter { it.mockTest.difficulty == TestDifficulty.HARD }
        else -> tests // Should ideally not happen with a fixed number of pages
    }
}

/**
 * Sorts a list of DisplayableMockTest objects based on the given criteria and order.
 *
 * @param tests The list of DisplayableMockTest objects to sort.
 * @param criteria The SortCriteria to use for sorting.
 * @param ascending True to sort in ascending order, false for descending.
 * @return A new sorted list of DisplayableMockTest objects.
 */
fun sortTests( // Renamed from sortDisplayableMockTests
    tests: List<DisplayableMockTest>,
    criteria: SortCriteria,
    ascending: Boolean
): List<DisplayableMockTest> {
    return when (criteria) {
        SortCriteria.MARKS -> {
            if (ascending) tests.sortedBy { it.latestScore }
            else tests.sortedByDescending { it.latestScore }
        }
        SortCriteria.DATE -> {
            if (ascending) tests.sortedBy { it.mockTest.creationDate }
            else tests.sortedByDescending { it.mockTest.creationDate }
        }
        SortCriteria.LAST_GIVEN -> {
            if (ascending) tests.sortedWith(compareBy(nullsLast()) { it.lastAttemptDate })
            else tests.sortedWith(compareByDescending(nullsFirst()) { it.lastAttemptDate })
        }
        SortCriteria.FORGETTING_CURVE -> {
            // Placeholder for forgetting curve logic - current fallback to DATE
            // This logic should be updated when actual forgetting curve calculation is implemented
            if (ascending) tests.sortedBy { it.mockTest.creationDate }
            else tests.sortedByDescending { it.mockTest.creationDate }
        }
        SortCriteria.NONE -> tests
    }
}

/**
 * Sorts a list of TestHistoryAttempt objects based on the given criteria and order.
 *
 * @param attempts The list of TestHistoryAttempt objects to sort.
 * @param sortBy The TestHistorySortCriteria to use for sorting.
 * @param sortAscending True to sort in ascending order, false for descending.
 * @return A new sorted list of TestHistoryAttempt objects.
 */
fun sortTestHistoryAttempts(
    attempts: List<AttemptWithTest>,
    sortBy: TestHistorySortCriteria, // Changed from String
    sortAscending: Boolean
): List<AttemptWithTest> {
    return when (sortBy) {
        TestHistorySortCriteria.DATE -> if (sortAscending) attempts.sortedBy { it.date } else attempts.sortedByDescending { it.date }
        TestHistorySortCriteria.SCORE -> if (sortAscending) attempts.sortedBy { it.score } else attempts.sortedByDescending { it.score }
        TestHistorySortCriteria.TEST_NAME -> if (sortAscending) attempts.sortedBy { it.testName.lowercase() } else attempts.sortedByDescending { it.testName.lowercase() }
        // No 'else -> attempts' needed as all enum cases are handled by exhaustive when,
        // or add it if TestHistorySortCriteria can be nullable or has an unhandled case.
    }
}
