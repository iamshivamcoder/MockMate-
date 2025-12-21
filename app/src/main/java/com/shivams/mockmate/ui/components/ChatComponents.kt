package com.shivams.mockmate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.model.ChatMessage
import com.shivams.mockmate.model.MessageType
import com.shivams.mockmate.model.QuickAction

/**
 * User message bubble (right-aligned, primary color)
 */
@Composable
fun UserMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Mentor message bubble (left-aligned, surface variant)
 */
@Composable
fun MentorMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Mentor avatar with gradient
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.messageType == MessageType.GREETING) {
                    Text(
                        text = "UPSC Mentor",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = message.content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Typing indicator with animated dots
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Mentor avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Card(
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val delay = index * 200
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = delay),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Quick action chips row
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionChips(
    actions: List<QuickAction>,
    onActionClick: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            AssistChip(
                onClick = { onActionClick(action) },
                label = { 
                    Text(
                        text = action.label,
                        fontSize = 12.sp
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

/**
 * Chat input field with send button
 */
@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        text = "Ask your mentor...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                enabled = enabled,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (value.isNotBlank()) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (value.isNotBlank()) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}

/**
 * Empty chat state
 */
@Composable
fun EmptyChatState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your AI Mentor",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "I'm here to help you with your UPSC preparation.\nAsk me anything!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Chat message item - dispatches to appropriate bubble type
 */
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it / 2 }
    ) {
        if (message.isFromUser) {
            UserMessageBubble(message = message, modifier = modifier)
        } else {
            MentorMessageBubble(message = message, modifier = modifier)
        }
    }
}
