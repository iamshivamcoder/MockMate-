package com.shivams.mockmate.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.shivams.mockmate.data.repositories.SettingsRepository
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.QuestionType
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.components.AppBottomNavigationBar
import com.shivams.mockmate.ui.screens.AboutDeveloperScreen
import com.shivams.mockmate.ui.screens.AnalyticsScreen
import com.shivams.mockmate.ui.screens.DashboardScreen
import com.shivams.mockmate.ui.screens.HelpScreen
import com.shivams.mockmate.ui.screens.MatchTheColumnScreen
import com.shivams.mockmate.ui.screens.MatchTheColumnSelectionScreen
import com.shivams.mockmate.ui.screens.MockTestSelectionScreen
import com.shivams.mockmate.ui.screens.NotificationDetailScreen
import com.shivams.mockmate.ui.screens.NotificationItemData
import com.shivams.mockmate.ui.screens.NotificationScreen
import com.shivams.mockmate.ui.screens.PracticeModeSelectionScreen
import com.shivams.mockmate.ui.screens.ProfileScreen
import com.shivams.mockmate.ui.screens.SettingsScreen
import com.shivams.mockmate.ui.screens.StreakScreen
import com.shivams.mockmate.ui.screens.TestHistoryScreen
import com.shivams.mockmate.ui.screens.TestImportScreen
import com.shivams.mockmate.ui.screens.TestResultScreen
import com.shivams.mockmate.ui.screens.TestTakingScreen
import com.shivams.mockmate.ui.util.ComposeStabilityUtils
import com.shivams.mockmate.ui.viewmodels.TestHistoryViewModel
import kotlinx.coroutines.launch
import java.util.Date

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
    const val ANALYTICS_SCREEN = "analytics_screen" // New route
    const val NOTIFICATION_SCREEN = "notification_screen"
    const val PROFILE_SCREEN = "profile_screen"
    const val NOTIFICATION_DETAIL_SCREEN = "notification_detail_screen/{notificationId}"
    const val STREAK_SCREEN = "streak_screen"

    fun testTakingRoute(testId: String) = "test_taking/$testId"
    fun testResultRoute(attemptId: String, testId: String) = "test_result/$attemptId/$testId"
    fun matchTheColumnTakingRoute(testId: String) = "match_the_column_taking/$testId"
    fun notificationDetailRoute(notificationId: String) = "notification_detail_screen/$notificationId"

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
    repository: TestRepository,
    settingsRepository: SettingsRepository
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
                    onAnalyticsClick = { navController.safeNavigate(Routes.ANALYTICS_SCREEN) },
                    onNotificationClick = { navController.safeNavigate(Routes.NOTIFICATION_SCREEN) },
                    onProfileClick = { navController.safeNavigate(Routes.PROFILE_SCREEN) },
                    onStreakClick = { navController.safeNavigate(Routes.STREAK_SCREEN) },
                    repository = stableRepository
                )
            }

            composable(Routes.ANALYTICS_SCREEN) { // New destination
                val userStats by stableRepository.userStats.collectAsState(initial = UserStatsDefaults.default()) // Provide a default UserStats
                val testAttempts by stableRepository.getAllTestAttempts().collectAsState(initial = emptyList())
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(userStats, testAttempts) {
                    isLoading = userStats == UserStatsDefaults.default() || testAttempts.isEmpty()
                }

                // Enhanced debugging for analytics data
                Log.d("Navigation", "Analytics Screen - Collected userStats: questionsAnswered=${userStats.questionsAnswered}, correctAnswers=${userStats.correctAnswers}, currentStreak=${userStats.currentStreak}")
                Log.d("Navigation", "Analytics Screen - Collected testAttempts: ${testAttempts.size} attempts")
                Log.d("Navigation", "Analytics Screen - Subject performance: ${userStats.subjectPerformance.size} subjects")
                testAttempts.forEach { attempt ->
                    Log.d("Navigation", "Analytics Screen - Attempt ${attempt.id}: score=${attempt.score}, completed=${attempt.isCompleted}, answers=${attempt.userAnswers.size}")
                }

                AnalyticsScreen(
                    userStats = userStats,
                    testAttempts = testAttempts,
                    isLoading = isLoading
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
                    onImportClick = { navController.safeNavigate(Routes.TEST_IMPORT) },
                    repository = stableRepository,
                    settingsRepository = settingsRepository
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
                        }
                        catch (e: Exception) {
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
                    onAnalyticsClick = { navController.safeNavigate(Routes.ANALYTICS_SCREEN) },
                    onTestHistoryClick = { navController.safeNavigate(Routes.TEST_HISTORY) },
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
                val viewModel: TestHistoryViewModel = hiltViewModel()
                TestHistoryScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onViewTestResult = { attemptId, testId ->
                        val route = Routes.testResultRoute(attemptId, testId)
                        Routes.logRoute(route)
                        navController.safeNavigate(route)
                    },
                    repository = stableRepository,
                    viewModel = viewModel
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

            composable(Routes.NOTIFICATION_SCREEN) {
                NotificationScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onNotificationClick = { notificationId ->
                        navController.safeNavigate(Routes.notificationDetailRoute(notificationId))
                    }
                )
            }

            composable(Routes.PROFILE_SCREEN) {
                ProfileScreen(onNavigateBack = { navController.navigateUp() })
            }

            composable(Routes.STREAK_SCREEN) {
                StreakScreen(
                    onNavigateBack = { navController.navigateUp() },
                    repository = stableRepository
                )
            }

            composable(
                route = Routes.NOTIFICATION_DETAIL_SCREEN,
                arguments = listOf(navArgument("notificationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val notificationId = backStackEntry.arguments?.getString("notificationId")
                val notification = notifications.find { it.id == notificationId }
                NotificationDetailScreen(
                    notification = notification,
                    onNavigateBack = { navController.navigateUp() })
            }
        }
    }
}

object UserStatsDefaults {
    fun default() = UserStats(
        questionsAnswered = 0,
        correctAnswers = 0,
        currentStreak = 0,
        longestStreak = 0,
        lastPracticeDate = Date(0),
        subjectPerformance = emptyMap()
    )
}

val notifications = mutableListOf(
    NotificationItemData(
        id = "1",
        icon = Icons.Default.Event,
        title = "New Mock Test Available!",
        subtitle = "Today, 10:30 AM",
        description = "A new full-length mock test for the upcoming UPSC Prelims is now available. The test covers General Studies Paper I, including History, Geography, Polity, Economy, and Current Affairs.\n\nDuration: 2 hours.\nTotal Questions: 100.",
        actionLabel = "Start Mock Test Now"
    ),
    NotificationItemData(
        id = "2",
        icon = Icons.Default.Schedule,
        title = "Daily Practice Reminder.",
        subtitle = "Yesterday, 6:00 PM",
        description = "Don't forget to maintain your streak! Take 10 minutes to practice today's questions.",
        actionLabel = null, // No button needed for this one
        isRead = true
    ),
    NotificationItemData(
        id = "3",
        icon = Icons.Default.Description,
        title = "Your History Report is Ready.",
        subtitle = "Yesterday, 12:00 PM",
        description = "Your detailed performance analysis for the last History mock test is now available. Review your strong and weak areas.",
        actionLabel = "View Report"
    )
)
