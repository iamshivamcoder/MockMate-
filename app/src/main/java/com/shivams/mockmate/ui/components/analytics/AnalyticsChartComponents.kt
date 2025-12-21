package com.shivams.mockmate.ui.components.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.UserStats
import java.util.Calendar
import kotlin.math.roundToInt

/**
 * Animated line chart for displaying performance trends
 */
@Composable
fun AnimatedLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxValue = (data.maxOrNull() ?: 100f).coerceAtLeast(100f)
        val minValue = 0f
        val range = maxValue - minValue
        
        val padding = 8.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        
        val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
        
        // Draw horizontal grid lines
        for (i in 0..4) {
            val y = padding + chartHeight * (1 - i / 4f)
            drawLine(
                color = surfaceVariant,
                start = Offset(padding, y),
                end = Offset(size.width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw line path
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = padding + stepX * index * animatedProgress.value
            val y = padding + chartHeight * (1 - (value - minValue) / range)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw points
        data.forEachIndexed { index, value ->
            val x = padding + stepX * index * animatedProgress.value
            val y = padding + chartHeight * (1 - (value - minValue) / range)
            
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Animated progress bar for subject performance
 */
@Composable
fun AnimatedProgressBar(
    label: String,
    progress: Float,
    color: Color
) {
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(animatedProgress.value * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.7f))
                        )
                    )
            )
        }
    }
}

/**
 * Weekly activity bar chart card
 */
@Composable
fun WeeklyActivityCard(testAttempts: List<TestAttempt>) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    val weeklyActivity = remember(testAttempts) {
        val calendar = Calendar.getInstance()
        val currentWeekStart = calendar.clone() as Calendar
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0)
        currentWeekStart.set(Calendar.MINUTE, 0)
        
        val activityPerDay = IntArray(7)
        testAttempts.forEach { attempt ->
            if (attempt.isCompleted) {
                val attemptCal = Calendar.getInstance()
                attemptCal.time = attempt.startTime
                if (attemptCal.timeInMillis >= currentWeekStart.timeInMillis) {
                    val dayOfWeek = attemptCal.get(Calendar.DAY_OF_WEEK)
                    val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                    if (index in 0..6) activityPerDay[index]++
                }
            }
        }
        activityPerDay.toList()
    }
    
    val maxActivity = weeklyActivity.maxOrNull()?.coerceAtLeast(1) ?: 1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "This Week's Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEachIndexed { index, day ->
                    AnimatedActivityBar(
                        day = day,
                        activity = weeklyActivity[index],
                        maxActivity = maxActivity
                    )
                }
            }
        }
    }
}

/**
 * Single animated activity bar
 */
@Composable
fun AnimatedActivityBar(
    day: String,
    activity: Int,
    maxActivity: Int
) {
    val heightFraction = if (activity > 0) activity.toFloat() / maxActivity else 0.1f
    val animatedHeight = remember { Animatable(0f) }
    
    LaunchedEffect(heightFraction) {
        animatedHeight.animateTo(
            targetValue = heightFraction,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }
    
    val barColor = if (activity > 0) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxSize(animatedHeight.value)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = day,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
        if (activity > 0) {
            Text(
                text = activity.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Performance trend card with line chart
 */
@Composable
fun PerformanceTrendCard(testAttempts: List<TestAttempt>, userStats: UserStats) {
    val completedAttempts = testAttempts.filter { it.isCompleted }.takeLast(7)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Recent Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (completedAttempts.isEmpty()) {
                Text(
                    text = "Complete tests to see your trend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                val scores = completedAttempts.map { attempt ->
                    val totalQuestions = attempt.userAnswers.size
                    if (totalQuestions > 0 && userStats.questionsAnswered > 0) {
                        ((userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100).coerceIn(0f, 100f)
                    } else {
                        0f
                    }
                }
                AnimatedLineChart(
                    data = scores,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Last ${completedAttempts.size} tests",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    val trend = if (scores.size >= 2) {
                        if (scores.last() > scores.first()) "↑ Improving" else if (scores.last() < scores.first()) "↓ Declining" else "→ Stable"
                    } else "→ Building data"
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            trend.startsWith("↑") -> Color(0xFF4CAF50)
                            trend.startsWith("↓") -> Color(0xFFF44336)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Subject performance card with animated progress bars
 */
@Composable
fun SubjectPerformanceCard(userStats: UserStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Subject Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            userStats.subjectPerformance.forEach { (subject, performance) ->
                AnimatedProgressBar(
                    label = subject,
                    progress = performance.accuracy,
                    color = getSubjectColor(subject)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Get a consistent color for a subject
 */
fun getSubjectColor(subject: String): Color {
    val colors = listOf(
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFFE91E63),
        Color(0xFF00BCD4)
    )
    return colors[subject.hashCode().mod(colors.size).let { if (it < 0) it + colors.size else it }]
}
