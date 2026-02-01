package com.shivams.mockmate.ui.components.analysis

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Smart Loading Screen for cloud-connected analysis.
 * Cycles through engaging status messages to keep users informed during long waits.
 * Handles Render free-tier cold starts (~60s).
 */
@Composable
fun SmartLoadingScreen(
    modifier: Modifier = Modifier
) {
    val statusMessages = listOf(
        "🧠 Waking up the AI Brain...",
        "📄 Uploading your PDF...",
        "🔍 Scanning annotation patterns...",
        "🎯 Detecting 'Concept Collapse'...",
        "💡 Identifying intuition markers...",
        "⚠️ Spotting silly mistakes...",
        "📊 Calculating your score...",
        "🎓 Formulating Mentor Feedback..."
    )
    
    var currentIndex by remember { mutableIntStateOf(0) }
    
    // Cycle through messages every 4 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentIndex = (currentIndex + 1) % statusMessages.size
        }
    }
    
    // Pulsing animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Pulsing brain icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667EEA).copy(alpha = 0.3f),
                                Color(0xFF764BA2).copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Analyzing",
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress indicator
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF667EEA),
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated status message
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith 
                    fadeOut(animationSpec = tween(500))
                },
                label = "statusMessage"
            ) { index ->
                Text(
                    text = statusMessages[index],
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "This may take up to a minute...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
