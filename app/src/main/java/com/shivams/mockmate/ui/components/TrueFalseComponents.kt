package com.shivams.mockmate.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.ui.util.HapticFeedbackManager
import kotlin.math.roundToInt

@Composable
fun SwipeableStatementCard(
    statement: String,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 150f
    
    val rotation by animateFloatAsState(
        targetValue = (offsetX / 20f).coerceIn(-15f, 15f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "rotation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (offsetY < -50f) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            offsetX > 50f -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            offsetX < -50f -> Color(0xFFF44336).copy(alpha = 0.2f)
            offsetY < -50f -> Color(0xFFFF9800).copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "backgroundColor"
    )
    
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SwipeDirectionIndicators(
            showTrue = offsetX > 50f,
            showFalse = offsetX < -50f,
            showSkip = offsetY < -50f
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .graphicsLayer { rotationZ = rotation; scaleX = scale; scaleY = scale }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThreshold -> onSwipeRight()
                                offsetX < -swipeThreshold -> onSwipeLeft()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount -> offsetX += dragAmount }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (offsetY < -swipeThreshold) onSwipeUp()
                            offsetY = 0f
                        },
                        onVerticalDrag = { _, dragAmount -> if (dragAmount < 0) offsetY += dragAmount }
                    )
                },
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statement,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SwipeDirectionIndicators(showTrue: Boolean, showFalse: Boolean, showSkip: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (showTrue) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("TRUE", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
        if (showFalse) {
            Box(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
                    .background(Color(0xFFF44336), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("FALSE", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
        if (showSkip) {
            Box(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
                    .background(Color(0xFFFF9800), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("SKIP", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun TrueFalseActionButtons(
    onTrue: () -> Unit,
    onFalse: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onFalse,
            modifier = Modifier.size(72.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF44336).copy(alpha = 0.15f))
        ) {
            Icon(Icons.Default.Close, contentDescription = "False", tint = Color(0xFFF44336), modifier = Modifier.size(36.dp))
        }
        IconButton(
            onClick = onSkip,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.15f))
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Skip", tint = Color(0xFFFF9800), modifier = Modifier.size(28.dp))
        }
        IconButton(
            onClick = onTrue,
            modifier = Modifier.size(72.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f))
        ) {
            Icon(Icons.Default.Check, contentDescription = "True", tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun AnswerFeedbackCard(
    isCorrect: Boolean,
    explanation: String,
    trapWords: List<String>,
    upscTip: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    
    // Trigger haptic feedback when card appears
    LaunchedEffect(Unit) {
        if (isCorrect) {
            HapticFeedbackManager.success(view)
        } else {
            HapticFeedbackManager.failure(view)
        }
    }
    
    val backgroundColor = if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f)
    val borderColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(borderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isCorrect) "Correct!" else "Incorrect",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = borderColor
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = explanation, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            
            if (trapWords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trap words: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                    Text(trapWords.joinToString(", "), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            if (upscTip.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(upscTip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
fun TrueFalseProgressIndicator(
    current: Int,
    total: Int,
    correctCount: Int,
    incorrectCount: Int,
    skippedCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Statement ${current + 1} of $total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatChip(count = correctCount, color = Color(0xFF4CAF50), label = "✓")
                MiniStatChip(count = incorrectCount, color = Color(0xFFF44336), label = "✗")
                MiniStatChip(count = skippedCount, color = Color(0xFFFF9800), label = "→")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxWidth((current + 1).toFloat() / total).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
        }
    }
}

@Composable
private fun MiniStatChip(count: Int, color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text("$count", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
    }
}
