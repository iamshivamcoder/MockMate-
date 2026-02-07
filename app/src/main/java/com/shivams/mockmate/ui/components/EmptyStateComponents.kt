package com.shivams.mockmate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Smart Empty State component with friendly messaging and call-to-action.
 * Makes empty screens feel like invitations rather than errors.
 */
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    primaryActionText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100) // Small delay for smooth entrance
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(400)
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                if (primaryActionText != null && onPrimaryAction != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onPrimaryAction,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(primaryActionText)
                    }
                }
                
                if (secondaryActionText != null && onSecondaryAction != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(secondaryActionText)
                    }
                }
            }
        }
    }
}

// ============================================
// Pre-built Empty State Variants
// ============================================

/**
 * Empty state for History screen - "No battles fought yet"
 */
@Composable
fun HistoryEmptyState(
    onStartMock: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateCard(
        icon = Icons.Filled.History,
        title = "No battles fought yet",
        subtitle = "Your test history will appear here once you complete your first mock test. Ready to begin?",
        primaryActionText = "Start First Mock",
        onPrimaryAction = onStartMock,
        modifier = modifier
    )
}

/**
 * Empty state for Search - "That topic is elusive"
 */
@Composable
fun SearchEmptyState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    EmptyStateCard(
        icon = Icons.Filled.Search,
        title = "That topic is elusive",
        subtitle = "No results found for \"$searchQuery\". Try searching for 'Polity', 'Economy', or 'History'.",
        modifier = modifier
    )
}

/**
 * Error state with retry - "Server needs a coffee break"
 */
@Composable
fun ErrorStateCard(
    onRetry: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    EmptyStateCard(
        icon = Icons.Filled.CloudOff,
        title = "The server needs a coffee break",
        subtitle = errorMessage ?: "Something went wrong. Don't worry, it happens to the best of us.",
        primaryActionText = "Try Again",
        onPrimaryAction = onRetry,
        modifier = modifier
    )
}

/**
 * Empty state for no tests available
 */
@Composable
fun NoTestsEmptyState(
    onImportTests: () -> Unit,
    onGenerateWithAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateCard(
        icon = Icons.Filled.Book,
        title = "No tests available",
        subtitle = "Create your own tests or import from JSON to get started.",
        primaryActionText = "Generate with AI",
        onPrimaryAction = onGenerateWithAi,
        secondaryActionText = "Import Tests",
        onSecondaryAction = onImportTests,
        modifier = modifier
    )
}

/**
 * Empty state for no bookmarks
 */
@Composable
fun NoBookmarksEmptyState(
    modifier: Modifier = Modifier
) {
    EmptyStateCard(
        icon = Icons.Filled.SentimentDissatisfied,
        title = "No bookmarks yet",
        subtitle = "Tap the bookmark icon on any question to save it for later review.",
        modifier = modifier
    )
}
