package com.example.mockmate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable // Added import
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment // Import for streak icon
import androidx.compose.material.icons.filled.MoreVert
// Imports from BottomNavigationBar.kt merged below
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
import androidx.compose.material3.TopAppBarScrollBehavior // Added import
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Added import for Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Added import for sp
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
    onStreakClick: (() -> Unit)? = null, // Added onStreakClick parameter
    dropdownContent: (@Composable ColumnScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null // Added scrollBehavior parameter
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = onStreakClick != null) { onStreakClick?.invoke() } // Made streak display clickable
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Current Streak",
                        tint = Color(0xFFFF4500).copy(alpha = iconAlpha), // Apply animated alpha
                        modifier = Modifier.size(24.dp) // Added size for consistency
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFF4500) // Changed to OrangeRed
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Spacing before other icons
                }
            }

            if (showSettings) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings, // Changed to Icons.Filled.Settings from Icons.Default.Settings for consistency
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
        scrollBehavior = scrollBehavior // Passed scrollBehavior to TopAppBar
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
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
