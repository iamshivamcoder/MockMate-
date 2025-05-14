package com.example.mockmate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mockmate.ui.screens.DashboardScreen
import com.example.mockmate.ui.screens.MockTestSelectionScreen
import com.example.mockmate.ui.screens.PracticeModeSelectionScreen
import com.example.mockmate.ui.screens.SettingsScreen
import com.example.mockmate.ui.screens.TestHistoryScreen
import com.example.mockmate.ui.screens.TestImportScreen
import com.example.mockmate.ui.screens.TestTakingScreen
import com.example.mockmate.ui.screens.TestResultScreen
import com.example.mockmate.data.TestRepository
import com.example.mockmate.MockMateApplication
import com.example.mockmate.ui.util.ComposeStabilityUtils
import android.util.Log
import androidx.compose.runtime.remember

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRACTICE_MODE_SELECTION = "practice_mode_selection"
    const val MOCK_TEST_SELECTION = "mock_test_selection"
    const val TEST_TAKING = "test_taking/{testId}"
    const val TEST_RESULT = "test_result/{attemptId}/{testId}"
    const val SETTINGS = "settings"
    const val TEST_HISTORY = "test_history"
    const val TEST_IMPORT = "test_import"
    
    // Helper functions to navigate with parameters
    fun testTakingRoute(testId: String) = "test_taking/$testId"
    fun testResultRoute(attemptId: String, testId: String) = "test_result/$attemptId/$testId"
    
    // Log navigation routes for debugging
    fun logRoute(route: String) {
        android.util.Log.d("Navigation", "Navigating to route: $route")
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
            TestTakingScreen(
                testId = testId,
                onNavigateBack = { navController.navigateUp() },
                onFinish = { attemptId -> 
                    val route = Routes.testResultRoute(attemptId, testId)
                    Routes.logRoute(route)
                    
                    try {
                        // Use popUpTo to ensure we don't have multiple test result screens in the stack
                        navController.navigate(route) {
                            // Clear back stack up to the test taking screen
                            popUpTo(Routes.TEST_TAKING.replace("{testId}", testId)) {
                                inclusive = true
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Navigation", "Error navigating to results: ${e.message}", e)
                    }
                },
                repository = stableRepository
            )
        }
        
        composable(
            route = Routes.TEST_RESULT,
            arguments = listOf(
                navArgument("attemptId") { type = NavType.StringType },
                navArgument("testId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString("attemptId") ?: ""
            val testId = backStackEntry.arguments?.getString("testId") ?: ""
            TestResultScreen(
                attemptId = attemptId,
                testId = testId,
                onNavigateBack = { navController.navigateUp() },
                onDashboardClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
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
                    navController.navigate(route)
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