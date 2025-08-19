package com.example.mockmate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column // Added import
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mockmate.ui.navigation.Screen

/**
 * A customizable top app bar for the MockMate application.
 *
 * @param title The title to display in the app bar.
 * @param showBackButton Whether to show a back button. Defaults to true.
 * @param showSettings Whether to show a settings icon. Defaults to false.
 * @param currentStreak The current streak count to display. If null or <= 0, it won't be shown.
 * @param onBackClick Lambda to be invoked when the back button is clicked.
 * @param onSettingsClick Lambda to be invoked when the settings icon is clicked.
 * @param onStreakClick Lambda to be invoked when the streak display is clicked.
 * @param onImportClick Lambda to be invoked when the import icon is clicked.
 * @param dropdownContent Optional composable content for a dropdown menu.
 * @param scrollBehavior Optional [TopAppBarScrollBehavior] to apply to the TopAppBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockMateTopBar(
    title: String,
    showBackButton: Boolean = true,
    showSettings: Boolean = false,
    currentStreak: Int? = null,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStreakClick: (() -> Unit)? = null,
    onImportClick: (() -> Unit)? = null,
    dropdownContent: (@Composable ColumnScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge, letterSpacing = 0.5.sp) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            // Display streak icon and count if currentStreak is available and positive
            if (currentStreak != null && currentStreak > 0) {
                val infiniteTransition = rememberInfiniteTransition(label = "streak_icon_animation")
                val iconAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 700, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "streak_icon_alpha"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(enabled = onStreakClick != null) { onStreakClick?.invoke() }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Current Streak",
                        tint = Color(0xFFFF4500).copy(alpha = iconAlpha),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFF4500)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp)) // Spacer after the streak Column
            }

            // Display import icon if onImportClick is provided
            onImportClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.Filled.FileUpload,
                        contentDescription = "Import Test"
                    )
                }
            }

            if (showSettings) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
            if (dropdownContent != null) {
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
                    dropdownContent()
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

// Content from BottomNavigationBar.kt starts here

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Dashboard", Icons.Filled.Dashboard, Screen.Dashboard.route),
        BottomNavItem("Practice", Icons.Filled.ModelTraining, Screen.PracticeModeSelection.route),
        BottomNavItem("History", Icons.Filled.History, Screen.TestHistory.route),
        BottomNavItem("Settings", Icons.Filled.Settings, Screen.Settings.route)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
