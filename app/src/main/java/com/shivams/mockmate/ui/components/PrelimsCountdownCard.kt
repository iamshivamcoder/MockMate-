
package com.shivams.mockmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

    LaunchedEffect(key1 = targetDate) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft = targetDate.timeInMillis - System.currentTimeMillis()
            showColon = !showColon
        }
    }

    val timeParts = remember(timeLeft) { calculateTimeParts(timeLeft) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (timeLeft > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${timeParts.days} Days | ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        Text(
                            text = "%02d".format(timeParts.hours),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (showColon) " : " else "   ",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        Text(
                            text = "%02d".format(timeParts.minutes),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (showColon) " : " else "   ",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        Text(
                            text = "%02d".format(timeParts.seconds),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = " left",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Text(
                    text = "🚀 Exam Day! Give it your best shot!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrelimsCountdownCardPreview() {
    MockMateTheme {
        PrelimsCountdownCard()
    }
}
