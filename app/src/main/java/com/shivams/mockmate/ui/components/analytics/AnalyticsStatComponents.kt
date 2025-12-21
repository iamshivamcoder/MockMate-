package com.shivams.mockmate.ui.components.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.UserStats
import kotlin.math.roundToInt

/**
 * Animated accuracy ring with gradient
 */
@Composable
fun AnimatedAccuracyRing(
    correctAnswers: Int,
    totalQuestions: Int
) {
    val accuracy = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(accuracy) {
        animatedProgress.animateTo(
            targetValue = accuracy,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Overall Accuracy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Background ring
                    drawCircle(
                        color = backgroundColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Progress ring with gradient
                    val sweepAngle = animatedProgress.value * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(primaryColor, secondaryColor, primaryColor),
                            center = center
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress.value * 100).roundToInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$correctAnswers / $totalQuestions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPill(
                    label = "Correct",
                    value = correctAnswers.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatPill(
                    label = "Incorrect",
                    value = (totalQuestions - correctAnswers).toString(),
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

/**
 * Small stat pill with label and value
 */
@Composable
fun StatPill(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * Quick stats row with animated stat cards
 */
@Composable
fun QuickStatsRow(
    testAttempts: List<TestAttempt>,
    userStats: UserStats
) {
    val avgScore = if (userStats.questionsAnswered > 0) {
        ((userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100).roundToInt()
    } else 0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.QuestionAnswer,
            value = testAttempts.size.toString(),
            label = "Tests Taken",
            color = Color(0xFF2196F3)
        )
        AnimatedStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Speed,
            value = "${avgScore}%",
            label = "Avg Score",
            color = Color(0xFF9C27B0)
        )
        AnimatedStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            value = userStats.currentStreak.toString(),
            label = "Streak",
            color = Color(0xFFFF6B35)
        )
    }
}

/**
 * Animated stat card with bounce effect
 */
@Composable
fun AnimatedStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Empty analytics state component
 */
@Composable
fun EmptyAnalyticsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No Analytics Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Complete some tests to see your\nperformance analytics here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
