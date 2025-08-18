package com.example.mockmate.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * A customizable top app bar for the MockMate application.
 *
 * @param title The title to display in the app bar.
 * @param showBackButton Whether to show a back button. Defaults to true.
 * @param showSettings Whether to show a settings icon. Defaults to false.
 * @param onBackClick Lambda to be invoked when the back button is clicked.
 * @param onSettingsClick Lambda to be invoked when the settings icon is clicked.
 * @param dropdownContent Optional composable content for a dropdown menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockMateTopBar(
    title: String,
    showBackButton: Boolean = true,
    showSettings: Boolean = false,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    dropdownContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
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
            if (showSettings) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
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
        }
    )
}
