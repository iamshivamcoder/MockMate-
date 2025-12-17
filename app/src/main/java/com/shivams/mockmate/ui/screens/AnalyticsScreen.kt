package com.shivams.mockmate.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.theme.MockMateTheme
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    userStats: UserStats,
    testAttempts: List<TestAttempt>,
    isLoading: Boolean
) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            showContent = true
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (userStats.questionsAnswered == 0 && testAttempts.isEmpty()) {
        EmptyAnalyticsState()
    } else {
        Log.d("AnalyticsScreen", "Rendering with ${testAttempts.size} attempts, ${userStats.questionsAnswered} questions answered")
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(300)) + slideInVertically(tween(400)) { -50 }
            ) {
                Text(
                    text = "Your Performance",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Animated accuracy ring
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 100)) + slideInVertically(tween(500, 100)) { -30 }
            ) {
                AnimatedAccuracyRing(
                    correctAnswers = userStats.correctAnswers,
                    totalQuestions = userStats.questionsAnswered
                )
            }
            
            // Quick stats row
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 200)) + slideInVertically(tween(500, 200)) { -30 }
            ) {
                QuickStatsRow(
                    testAttempts = testAttempts,
                    userStats = userStats
                )
            }
            
            // Performance chart
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 300)) + slideInVertically(tween(500, 300)) { -30 }
            ) {
                PerformanceTrendCard(testAttempts = testAttempts)
            }
            
            // Subject performance
            if (userStats.subjectPerformance.isNotEmpty()) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400, 400)) + slideInVertically(tween(500, 400)) { -30 }
                ) {
                    SubjectPerformanceCard(userStats = userStats)
                }
            }
            
            // Weekly activity
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 500)) + slideInVertically(tween(500, 500)) { -30 }
            ) {
                WeeklyActivityCard(testAttempts = testAttempts)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EmptyAnalyticsState() {
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

@Composable
private fun AnimatedAccuracyRing(
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

@Composable
private fun StatPill(
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

@Composable
private fun QuickStatsRow(
    testAttempts: List<TestAttempt>,
    userStats: UserStats
) {
    val avgScore = if (testAttempts.isNotEmpty()) {
        testAttempts.filter { it.isCompleted }.map { it.score }.average().roundToInt()
    } else 0
    
    val totalTimeMinutes = testAttempts.sumOf { attempt ->
        attempt.userAnswers.values.sumOf { it.timeSpent }
    } / 60
    
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

@Composable
private fun AnimatedStatCard(
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

@Composable
private fun PerformanceTrendCard(testAttempts: List<TestAttempt>) {
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
                // Animated line chart
                val scores = completedAttempts.map { it.score.coerceIn(0f, 100f) }
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

@Composable
private fun AnimatedLineChart(
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

@Composable
private fun SubjectPerformanceCard(userStats: UserStats) {
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

@Composable
private fun AnimatedProgressBar(
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

@Composable
private fun WeeklyActivityCard(testAttempts: List<TestAttempt>) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    // Calculate activity for each day of current week
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

@Composable
private fun AnimatedActivityBar(
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

private fun getSubjectColor(subject: String): Color {
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

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    MockMateTheme {
        AnalyticsScreen(
            userStats = UserStats(
                questionsAnswered = 50,
                correctAnswers = 35,
                currentStreak = 5
            ),
            testAttempts = emptyList(),
            isLoading = false
        )
    }
}
