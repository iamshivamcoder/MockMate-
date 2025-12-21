package com.shivams.mockmate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.ui.theme.MockMateTheme
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.concurrent.TimeUnit

private data class TimeParts(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

private fun calculateTimeParts(millis: Long): TimeParts {
    if (millis <= 0) return TimeParts(0, 0, 0, 0)
    return TimeParts(
        days = TimeUnit.MILLISECONDS.toDays(millis),
        hours = TimeUnit.MILLISECONDS.toHours(millis) % 24,
        minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
        seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    )
}

@Composable
fun PrelimsCountdownCard(
    title: String = "UPSC Prelims 2026",
    targetDate: Calendar = remember {
        Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 24, 0, 0, 0)
        }
    }
) {
    var timeLeft by remember { mutableStateOf(targetDate.timeInMillis - System.currentTimeMillis()) }
    var showColon by remember { mutableStateOf(true) }
    
    // Pulse animation for urgency
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(key1 = targetDate) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft = targetDate.timeInMillis - System.currentTimeMillis()
            showColon = !showColon
        }
    }

    val timeParts = remember(timeLeft) { calculateTimeParts(timeLeft) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1A237E),
                            Color(0xFF3949AB)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (timeLeft > 0) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CountdownUnit(
                            value = timeParts.days.toInt(),
                            label = "DAYS",
                            modifier = Modifier.scale(pulseScale)
                        )
                        CountdownSeparator(showColon)
                        CountdownUnit(
                            value = timeParts.hours.toInt(),
                            label = "HRS"
                        )
                        CountdownSeparator(showColon)
                        CountdownUnit(
                            value = timeParts.minutes.toInt(),
                            label = "MIN"
                        )
                        CountdownSeparator(showColon)
                        CountdownUnit(
                            value = timeParts.seconds.toInt(),
                            label = "SEC"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Every hour counts! Keep preparing 💪",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "🚀 Exam Day! Give it your best shot!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownUnit(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "%02d".format(value),
                fontFamily = FontFamily.Monospace,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun CountdownSeparator(show: Boolean) {
    Text(
        text = if (show) ":" else " ",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PrelimsCountdownCardPreview() {
    MockMateTheme {
        PrelimsCountdownCard()
    }
}
