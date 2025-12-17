package com.shivams.mockmate.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.components.MockMateTopBar
import java.util.Calendar
import java.util.Date

/**
 * Streak Screen displaying user's current streak, weekly progress, and streak statistics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository
) {
    val userStats by repository.userStats.collectAsState(initial = UserStats())
    val testAttempts by repository.getAllTestAttempts().collectAsState(initial = emptyList())
    
    // Calculate total practice days from test attempts
    val totalPracticeDays = remember(testAttempts) {
        testAttempts
            .filter { it.isCompleted }
            .map { attempt ->
                val cal = Calendar.getInstance()
                cal.time = attempt.startTime
                Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            }
            .distinct()
            .size
    }
    
    // Calculate which days of current week have practice
    val weeklyPractice = remember(testAttempts) {
        val calendar = Calendar.getInstance()
        val currentWeekStart = calendar.clone() as Calendar
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0)
        currentWeekStart.set(Calendar.MINUTE, 0)
        currentWeekStart.set(Calendar.SECOND, 0)
        
        val practiceDays = mutableSetOf<Int>()
        testAttempts.forEach { attempt ->
            if (attempt.isCompleted) {
                val attemptCal = Calendar.getInstance()
                attemptCal.time = attempt.startTime
                
                // Check if attempt is in current week
                if (attemptCal.timeInMillis >= currentWeekStart.timeInMillis) {
                    val dayOfWeek = attemptCal.get(Calendar.DAY_OF_WEEK)
                    // Convert to 0-6 (Mon-Sun)
                    val normalizedDay = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                    practiceDays.add(normalizedDay)
                }
            }
        }
        practiceDays
    }
    
    // Get current day of week (0-6, Mon-Sun)
    val currentDayOfWeek = remember {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
    }
    
    Scaffold(
        topBar = {
            MockMateTopBar(
                title = "Your Streak",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current Streak Card
            CurrentStreakCard(
                currentStreak = userStats.currentStreak
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Weekly Progress Card
            WeeklyProgressCard(
                weeklyPractice = weeklyPractice,
                currentDayOfWeek = currentDayOfWeek,
                currentStreak = userStats.currentStreak
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Streak Stats Section
            Text(
                text = "STREAK STATS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "LONGEST\nSTREAK",
                    value = userStats.longestStreak,
                    unit = "DAYS",
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier.weight(1f)
                )
                
                StatCard(
                    title = "TOTAL\nPRACTICE DAYS",
                    value = totalPracticeDays,
                    unit = "DAYS",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Motivational Message
            MotivationalMessage(
                currentStreak = userStats.currentStreak,
                hasPracticedToday = weeklyPractice.contains(currentDayOfWeek)
            )
        }
    }
}

@Composable
private fun CurrentStreakCard(
    currentStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CURRENT STREAK",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DAYS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            
            // Fire Emoji
            Text(
                text = "🔥",
                fontSize = 80.sp
            )
        }
    }
}

@Composable
private fun WeeklyProgressCard(
    weeklyPractice: Set<Int>,
    currentDayOfWeek: Int,
    currentStreak: Int
) {
    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Day indicators (fire/check/empty)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEachIndexed { index, _ ->
                    DayIndicator(
                        isPracticed = weeklyPractice.contains(index),
                        isToday = index == currentDayOfWeek,
                        isPast = index < currentDayOfWeek
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            WeeklyProgressBar(
                practicedDays = weeklyPractice.size,
                currentDayOfWeek = currentDayOfWeek
            )
        }
    }
}

@Composable
private fun DayIndicator(
    isPracticed: Boolean,
    isToday: Boolean,
    isPast: Boolean
) {
    val orangeColor = Color(0xFFFF6B35)
    val greenColor = Color(0xFF4CAF50)
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(
                if (isToday && !isPracticed) {
                    Modifier.border(2.dp, orangeColor, CircleShape)
                } else {
                    Modifier
                }
            )
            .background(
                when {
                    isPracticed -> greenColor.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            isPracticed -> {
                // Show checkmark with fire for practiced days
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Practiced",
                        tint = greenColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            isPast -> {
                // Show fire for missed past days (indicating streak was active)
                Text(
                    text = "🔥",
                    fontSize = 20.sp
                )
            }
            isToday -> {
                // Empty circle for today if not practiced
                Text(
                    text = "🔥",
                    fontSize = 20.sp,
                    color = Color.Gray.copy(alpha = 0.5f).let { Color.Unspecified }
                )
            }
            else -> {
                // Future days - empty
            }
        }
    }
}

@Composable
private fun WeeklyProgressBar(
    practicedDays: Int,
    currentDayOfWeek: Int
) {
    val orangeColor = Color(0xFFFF6B35)
    val yellowColor = Color(0xFFFFB74D)
    
    var animatedProgress by remember { mutableStateOf(0f) }
    val targetProgress = if (currentDayOfWeek + 1 > 0) {
        practicedDays.toFloat() / 7f
    } else 0f
    
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1000),
        label = "progress"
    )
    
    LaunchedEffect(targetProgress) {
        animatedProgress = targetProgress
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedValue)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(orangeColor, yellowColor)
                    )
                )
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: Int,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MotivationalMessage(
    currentStreak: Int,
    hasPracticedToday: Boolean
) {
    val message = when {
        hasPracticedToday && currentStreak >= 7 -> "Amazing! You're on fire! Keep the momentum going! 🔥"
        hasPracticedToday && currentStreak >= 3 -> "Great job today! Your consistency is paying off! 💪"
        hasPracticedToday -> "Well done! You've practiced today. Keep building your streak! ⭐"
        currentStreak > 0 -> "Keep it up! Practice tomorrow to maintain your streak."
        else -> "Start your streak today! Every journey begins with a single step. 🚀"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
