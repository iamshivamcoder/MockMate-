package com.shivams.mockmate.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WelcomeCard(userName: String) {
    val greeting = remember { getGreeting() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (userName.isNotEmpty()) "$greeting $userName!" else "$greeting!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ready to continue your UPSC preparation journey?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date()) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetings = listOf(
        Pair(0..11, listOf(
            "Namaste, Early Bird",
            "Good Morning, Warrior",
            "Rise and Shine, Champion",
            "Suprabhat, Learner"
        )),
        Pair(12..15, listOf(
            "Namaste, Determined Soul",
            "Keep Going, Warrior",
            "Stay Strong, Fighter",
            "Power Through, Champion"
        )),
        Pair(16..19, listOf(
            "Good Evening, Achiever",
            "Pushing Forward, Warrior",
            "Stay Focused, Champion",
            "Almost There, Fighter"
        )),
        Pair(20..23, listOf(
            "Keep At It, Night Owl",
            "Burning Midnight Oil, Warrior",
            "Dedication Personified",
            "Night Mode: Activated"
        ))
    )

    val timeSlot = greetings.find { (range, _) -> hour in range }
    val greetingList = timeSlot?.second ?: greetings[0].second
    return greetingList.random()
}
