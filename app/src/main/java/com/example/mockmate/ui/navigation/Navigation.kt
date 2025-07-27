package com.example.mockmate.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mockmate.MockMateApplication
import com.example.mockmate.data.TestRepository
import com.example.mockmate.ui.screens.DashboardScreen
import com.example.mockmate.ui.screens.MockTestSelectionScreen
import com.example.mockmate.ui.screens.PracticeModeSelectionScreen
import com.example.mockmate.ui.screens.SettingsScreen
import com.example.mockmate.ui.screens.TestHistoryScreen
import com.example.mockmate.ui.screens.TestImportScreen
import com.example.mockmate.ui.screens.TestResultScreen
import com.example.mockmate.ui.screens.TestTakingScreen
import com.example.mockmate.ui.util.ComposeStabilityUtils

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRACTICE_MODE_SELECTION = "practice_mode_selection"
    const val MOCK_TEST_SELECTION = "mock_test_selection"
    const val TEST_TAKING = "test_taking/{testId}"
    const val TEST_RESULT = "test_result/{attemptId}/{testId}"
    const val SETTINGS = "settings"
    const val TEST_HISTORY = "test_history"
    const val TEST_IMPORT = "test_import"
    const val PARAGRAPH_ANALYSIS = "paragraph_analysis"

    // Helper functions to navigate with parameters
    fun testTakingRoute(testId: String) = "test_taking/$testId"
    fun testResultRoute(attemptId: String, testId: String) = "test_result/$attemptId/$testId"
    
    // Log navigation routes for debugging
    fun logRoute(route: String) {
        android.util.Log.d("Navigation", "Navigating to route: $route")
    }
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
        // Fallback navigation to ensure user isn't stuck
        try {
            popBackStack()
            navigate(Routes.DASHBOARD)
        } catch (e: Exception) {
            Log.e("Navigation", "Even fallback navigation failed: ${e.message}", e)
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = Routes.DASHBOARD, repository: TestRepository = MockMateApplication.getTestRepository()) {
    // Add stability monitoring
    ComposeStabilityUtils.LogCompositionErrors("AppNavHost")
    ComposeStabilityUtils.MonitorLifecycle { errorMsg ->
        Log.e("Navigation", "Lifecycle error: $errorMsg")
    }
    
    // Use a stable repository reference that won't change during recompositions
    val stableRepository = remember { repository }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.DASHBOARD) {
            val context = LocalContext.current
            DashboardScreen(
                onPracticeClick = { navController.navigate(Routes.PRACTICE_MODE_SELECTION) },
                onHistoryClick = { navController.navigate(Routes.TEST_HISTORY) },
                onImportClick = { navController.navigate(Routes.TEST_IMPORT) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        
        composable(Routes.PRACTICE_MODE_SELECTION) {
            PracticeModeSelectionScreen(
                onNavigateBack = { navController.navigateUp() },
                onMockTestClick = { navController.navigate(Routes.MOCK_TEST_SELECTION) },
                onParagraphAnalysisClick = { navController.navigate(Routes.PARAGRAPH_ANALYSIS) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        
        composable(Routes.MOCK_TEST_SELECTION) {
            MockTestSelectionScreen(
                onNavigateBack = { navController.navigateUp() },
                onTestSelected = { testId -> navController.navigate(Routes.testTakingRoute(testId)) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
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
                    navController.safeNavigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
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
            route = Routes.TEST_RESULT,
            arguments = listOf(
                navArgument("attemptId") {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("testId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString("attemptId")
            val testId = backStackEntry.arguments?.getString("testId")

            if (attemptId == null || testId == null) {
                Log.e("Navigation", "Missing required arguments for TestResultScreen")
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
                return@composable
            }

            TestResultScreen(
                attemptId = attemptId,
                testId = testId,
                onNavigateBack = { navController.navigateUp() },
                onDashboardClick = {
                    navController.safeNavigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                repository = stableRepository
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }


        // Test History Screen
        composable(Routes.TEST_HISTORY) {
            TestHistoryScreen(
                onNavigateBack = { navController.navigateUp() },
                onViewTestResult = { attemptId, testId ->
                    // Navigate to test result screen
                    val route = Routes.testResultRoute(attemptId, testId)
                    Routes.logRoute(route)
                    navController.safeNavigate(route)
                },
                repository = stableRepository
            )
        }
        
        // Test Import Screen
        composable(Routes.TEST_IMPORT) {
            TestImportScreen(
                onNavigateBack = { navController.navigateUp() },
                onViewTests = { navController.navigate(Routes.MOCK_TEST_SELECTION) },
                repository = stableRepository
            )
        }
    }
}