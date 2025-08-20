package com.example.mockmate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExpandableContentCard(
    title: String,
    leadingIcon: ImageVector? = null,
    initiallyExpanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    var isClickable by remember { mutableStateOf(true) }
    val hapticFeedback = LocalHapticFeedback.current

    // Debounce rapid clicks
    suspend fun toggleWithDebounce() {
        if (!isClickable || !enabled) return
        isClickable = false

        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        isExpanded = !isExpanded
        onExpandedChange?.invoke(isExpanded)

        delay(300) // Prevent rapid toggling
        isClickable = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), // This is external padding, remains the same
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .clickable(enabled = enabled) {
                    if (isClickable) {
                        kotlinx.coroutines.MainScope().launch {
                            toggleWithDebounce()
                        }
                    }
                }
                .focusable()
                .semantics {
                    stateDescription = if (isExpanded) "Expanded" else "Collapsed"
                    role = Role.Button
                    onClick {
                        if (enabled && isClickable) {
                            kotlinx.coroutines.MainScope().launch {
                                toggleWithDebounce()
                            }
                        }
                        true
                    }
                }
                .padding(24.dp) // Increased from 16.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null, // Title already provides context
                        tint = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(24.dp) // Increased from 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp)) // Increased from 8.dp
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp)) // Increased from 8.dp

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), // Increased from 16.dp
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse $title" else "Expand $title",
                        tint = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        // Consider adding .size() here if default is too small now
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded && !isLoading,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300, delayMillis = 100)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 250)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200)
                )
            ) {
                Column(
                    modifier = Modifier.semantics {
                        contentDescription = "$title content"
                    }
                ) {
                    Spacer(modifier = Modifier.height(12.dp)) // Increased from 8.dp
                    content()
                }
            }
        }
    }
}

// Extension function for better state management
@Composable
fun rememberExpandableCardState(
    key: String,
    initialExpanded: Boolean = false
): MutableState<Boolean> {
    return remember(key) { mutableStateOf(initialExpanded) }
}

@Preview
@Composable
fun ExpandableContentCardPreview() {
    ExpandableContentCard(
        title = "Sample Title",
        leadingIcon = Icons.Filled.KeyboardArrowDown,
    ) {
        Text("This is the expandable content of the card.")
    }
}
