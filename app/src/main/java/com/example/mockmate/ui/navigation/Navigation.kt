package com.example.mockmate.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.mockmate.MockMateApplication
import com.example.mockmate.data.TestRepository
import com.example.mockmate.model.QuestionType
import com.example.mockmate.ui.components.AppBottomNavigationBar
import com.example.mockmate.ui.screens.AboutDeveloperScreen
import com.example.mockmate.ui.screens.DashboardScreen
import com.example.mockmate.ui.screens.HelpScreen
import com.example.mockmate.ui.screens.MatchTheColumnScreen
import com.example.mockmate.ui.screens.MatchTheColumnSelectionScreen
import com.example.mockmate.ui.screens.MockTestSelectionScreen
import com.example.mockmate.ui.screens.PracticeModeSelectionScreen
import com.example.mockmate.ui.screens.SettingsScreen
import com.example.mockmate.ui.screens.TestHistoryScreen
import com.example.mockmate.ui.screens.TestImportScreen
import com.example.mockmate.ui.screens.TestResultScreen
import com.example.mockmate.ui.screens.TestTakingScreen
import com.example.mockmate.ui.util.ComposeStabilityUtils
import kotlinx.coroutines.launch

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRACTICE_MODE_SELECTION = "practice_mode_selection"
    const val MOCK_TEST_SELECTION = "mock_test_selection"
    const val MATCH_THE_COLUMN_SELECTION = "match_the_column_selection"
    const val TEST_TAKING = "test_taking/{testId}"
    const val TEST_RESULT = "test_result/{attemptId}/{testId}"
    const val SETTINGS = "settings"
    const val TEST_HISTORY = "test_history"
    const val TEST_IMPORT = "test_import"
    const val MATCH_THE_COLUMN_OLD = "match_the_column_old" // Renamed old route
    const val MATCH_THE_COLUMN_TAKING = "match_the_column_taking/{testId}"
    const val ABOUT_DEVELOPER = "about_developer"
    const val HELP_AND_FAQ = "help_and_faq"

    fun testTakingRoute(testId: String) = "test_taking/$testId"
    fun testResultRoute(attemptId: String, testId: String) = "test_result/$attemptId/$testId"
    fun matchTheColumnTakingRoute(testId: String) = "match_the_column_taking/$testId"

    fun logRoute(route: String) {
        Log.d("Navigation", "Navigating to route: $route")
    }
}

sealed class Screen(val route: String) {
    object Dashboard : Screen(Routes.DASHBOARD)
    object PracticeModeSelection : Screen(Routes.PRACTICE_MODE_SELECTION)
    object TestHistory : Screen(Routes.TEST_HISTORY)
    object Settings : Screen(Routes.SETTINGS)
    // Add other screens here if needed for typed navigation
}

private fun NavHostController.safeNavigate(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    try {
        Log.d("Navigation", "Attempting to navigate to: $route")
        navigate(route, builder)
        Log.d("Navigation", "Successfully navigated to: $route")
    } catch (e: Exception) {
        Log.e("Navigation", "Navigation failed to $route: ${e.message}", e)
        try {
            popBackStack()
            navigate(Routes.DASHBOARD)
        } catch (ef: Exception) {
            Log.e("Navigation", "Even fallback navigation failed: ${ef.message}", ef)
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.DASHBOARD,
    repository: TestRepository = MockMateApplication.getTestRepository()
) {
    ComposeStabilityUtils.LogCompositionErrors("AppNavHost")
    ComposeStabilityUtils.MonitorLifecycle { errorMsg ->
        Log.e("Navigation", "Lifecycle error: $errorMsg")
    }

    val stableRepository = remember { repository }
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(
        Screen.Dashboard.route,
        Screen.PracticeModeSelection.route,
        Screen.TestHistory.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarScreens) {
                AppBottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding) // Apply padding
        ) {
            composable(Routes.DASHBOARD) {
                LocalContext.current // Keep this if needed for other reasons
                DashboardScreen(
                    onPracticeClick = { navController.safeNavigate(Routes.PRACTICE_MODE_SELECTION) },
                    onHistoryClick = { navController.safeNavigate(Routes.TEST_HISTORY) },
                    onImportClick = { navController.safeNavigate(Routes.TEST_IMPORT) },
                    onSettingsClick = { navController.safeNavigate(Routes.SETTINGS) },
                    onAnalyticsClick = TODO(),
                    repository = TODO()
                )
            }

            composable(Routes.PRACTICE_MODE_SELECTION) {
                PracticeModeSelectionScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onMockTestClick = { navController.safeNavigate(Routes.MOCK_TEST_SELECTION) },
                    onParagraphAnalysisClick = { navController.safeNavigate(Routes.MATCH_THE_COLUMN_SELECTION) },
                    onSettingsClick = { navController.safeNavigate(Routes.SETTINGS) }
                )
            }

            composable(Routes.MOCK_TEST_SELECTION) {
                MockTestSelectionScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onTestSelected = { testId ->
                        coroutineScope.launch {
                            try {
                                val test = stableRepository.getTestById(testId)
                                if (test != null) {
                                    if (test.questions.firstOrNull()?.type == QuestionType.MATCH_THE_COLUMN) {
                                        navController.safeNavigate(Routes.matchTheColumnTakingRoute(testId))
                                    } else {
                                        navController.safeNavigate(Routes.testTakingRoute(testId))
                                    }
                                } else {
                                    Log.e("Navigation", "Test with ID $testId not found.")
                                    navController.popBackStack()
                                }
                            } catch (e: Exception) {
                                Log.e("Navigation", "Error fetching test $testId: ${e.message}", e)
                                navController.popBackStack()
                            }
                        }
                    },
                    onSettingsClick = { navController.safeNavigate(Routes.SETTINGS) },
                    repository = stableRepository
                )
            }

            composable(Routes.MATCH_THE_COLUMN_SELECTION) { // New composable entry
                MatchTheColumnSelectionScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onTestSelected = { testId ->
                        navController.safeNavigate(Routes.matchTheColumnTakingRoute(testId))
                    },
                    onSettingsClick = { navController.safeNavigate(Routes.SETTINGS) },
                    repository = stableRepository
                )
            }

            composable(
                route = Routes.TEST_TAKING,
                arguments = listOf(navArgument("testId") { type = NavType.StringType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getString("testId") ?: ""
                if (testId.isEmpty()) {
                    Log.e("Navigation", "Missing required testId argument for TestTakingScreen")
                    LaunchedEffect(Unit) {
                        navController.safeNavigate(Routes.DASHBOARD) { popUpTo(Routes.DASHBOARD) { inclusive = true } }
                    }
                    return@composable
                }
                TestTakingScreen(
                    testId = testId,
                    onNavigateBack = { navController.popBackStack() },
                    onFinish = { attemptId ->
                        Log.d("Navigation", "onFinish called with attemptId=$attemptId, testId=$testId")
                        try {
                            val route = Routes.testResultRoute(attemptId, testId)
                            Routes.logRoute(route)
                            navController.safeNavigate(route) {
                                popUpTo(Routes.testTakingRoute(testId)) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Log.e("Navigation", "Failed to navigate to test result: ${e.message}", e)
                            navController.safeNavigate(Routes.TEST_HISTORY)
                        }
                    },
                    repository = stableRepository
                )
            }

            composable(
                route = Routes.MATCH_THE_COLUMN_TAKING,
                arguments = listOf(navArgument("testId") { type = NavType.StringType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getString("testId")
                if (testId.isNullOrEmpty()) {
                    Log.e("Navigation", "Missing testId for MatchTheColumnScreen")
                    LaunchedEffect(Unit) { // Use LaunchedEffect for side effects in composables
                        navController.popBackStack() // Go back if no ID
                    }
                    return@composable // Prevent further composition
                }
                MatchTheColumnScreen(
                    onNavigateBack = { navController.navigateUp() },
                    testId = testId // Pass the testId here
                )
            }

            composable(
                route = Routes.TEST_RESULT,
                arguments = listOf(
                    navArgument("attemptId") { type = NavType.StringType; nullable = false },
                    navArgument("testId") { type = NavType.StringType; nullable = false }
                )
            ) { backStackEntry ->
                val attemptId = backStackEntry.arguments?.getString("attemptId")
                val testId = backStackEntry.arguments?.getString("testId")
                if (attemptId == null || testId == null) {
                    Log.e("Navigation", "Missing required arguments for TestResultScreen")
                    LaunchedEffect(Unit) {
                        navController.safeNavigate(Routes.DASHBOARD) { popUpTo(Routes.DASHBOARD) { inclusive = true } }
                    }
                    return@composable
                }
                TestResultScreen(
                    attemptId = attemptId,
                    testId = testId,
                    onNavigateBack = { navController.navigateUp() },
                    onDashboardClick = { navController.safeNavigate(Routes.DASHBOARD) { popUpTo(Routes.DASHBOARD) { inclusive = true } } },
                    repository = stableRepository
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToAboutDeveloper = { navController.safeNavigate(Routes.ABOUT_DEVELOPER) },
                    onNavigateToHelp = { navController.safeNavigate(Routes.HELP_AND_FAQ) }
                )
            }

            composable(Routes.TEST_HISTORY) {
                TestHistoryScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onViewTestResult = { attemptId, testId ->
                        val route = Routes.testResultRoute(attemptId, testId)
                        Routes.logRoute(route)
                        navController.safeNavigate(route)
                    },
                    repository = stableRepository
                )
            }

            composable(Routes.TEST_IMPORT) {
                TestImportScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onViewTests = { navController.safeNavigate(Routes.MOCK_TEST_SELECTION) },
                    repository = stableRepository
                )
            }

            composable(Routes.MATCH_THE_COLUMN_OLD) { // Original MatchTheColumn, no testId
                MatchTheColumnScreen(
                    onNavigateBack = { navController.navigateUp() }
                    // testId is null by default in MatchTheColumnScreen
                )
            }

            composable(Routes.ABOUT_DEVELOPER) {
                AboutDeveloperScreen(onNavigateBack = { navController.navigateUp() })
            }

            composable(Routes.HELP_AND_FAQ) {
                HelpScreen(onNavigateBack = { navController.navigateUp() })
            }
        }
    }
}
