package com.shivams.mockmate.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
// import androidx.compose.foundation.layout.fillMaxSize // Reverted
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.PracticeMode
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.theme.extendedColorScheme
import com.shivams.mockmate.util.DateFormatUtils
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A card component to represent a practice mode.
 *
 * @param mode The [PracticeMode] to display.
 * @param title The title of the practice mode.
 * @param description A short description of the practice mode.
 * @param onClick Lambda to be invoked when the card is clicked.
 * @param modifier Optional [Modifier] for this component.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PracticeModeCard(
    mode: PracticeMode,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = onClick), // Using combinedClickable here too for consistency, though only onClick is used
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconColor = when (mode) {
                PracticeMode.DAILY_CHALLENGE -> MaterialTheme.colorScheme.tertiary
                PracticeMode.FOCUSED_PRACTICE -> MaterialTheme.colorScheme.primary
                PracticeMode.CUSTOM_PRACTICE -> MaterialTheme.colorScheme.secondary
                PracticeMode.MOCK_TEST -> MaterialTheme.extendedColorScheme.mockTestColor
                PracticeMode.PARAGRAPH_ANALYSIS -> MaterialTheme.extendedColorScheme.paragraphAnalysisColor
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f))
                    .border(2.dp, iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (mode) {
                        PracticeMode.DAILY_CHALLENGE -> "DC"
                        PracticeMode.FOCUSED_PRACTICE -> "FP"
                        PracticeMode.CUSTOM_PRACTICE -> "CP"
                        PracticeMode.MOCK_TEST -> "MT"
                        PracticeMode.PARAGRAPH_ANALYSIS -> "PA"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to $title",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * A card component to display information about a mock test.
 *
 * @param test The [MockTest] data to display.
 * @param onClick Lambda to be invoked when the card is clicked.
 * @param onLongClick Optional lambda to be invoked on long click.
 * @param modifier Optional [Modifier] for this component.
 * @param pulsateBadges Whether the difficulty badge should pulsate, determined by user preference.
 */
@OptIn(ExperimentalFoundationApi::class) // Added for combinedClickable
@Composable
fun TestCard(
    test: MockTest,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    pulsateBadges: Boolean = false // Added new parameter
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = test.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${test.questions.size} Questions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${test.timeLimit} Minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                DifficultyBadge(
                    difficulty = test.difficulty,
                    isPulsating = pulsateBadges // Changed from hardcoded value
                ) // DifficultyBadge is in BadgeComponents.kt
            }
        }
    }
}

@Composable
fun UserStatsSection(userStats: UserStats) {
    SectionHeader(text = "Your Progress")

    val accuracy = if (userStats.questionsAnswered > 0) {
        userStats.correctAnswers.toFloat() / userStats.questionsAnswered
    } else {
        0f
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(
                title = "Questions Attempted",
                value = userStats.questionsAnswered.toString(),
                modifier = Modifier.weight(1f)
            )
            StatColumn(
                title = "Current Streak",
                value = "${userStats.currentStreak} days",
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Overall Accuracy: ${(accuracy * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProgressBar(progress = accuracy)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Correct Answers: ${userStats.correctAnswers} out of ${userStats.questionsAnswered}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Focus on consistent daily practice to improve your UPSC preparation!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StatColumn(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * A list of motivational quotes with their authors.
 */
private val motivationalQuotes = listOf(
    Pair("Success is not final, failure is not fatal: It is the courage to continue that counts.", "Winston Churchill"),
    Pair("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt"),
    Pair("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
    Pair("Everything you've ever wanted is on the other side of fear.", "George Addair"),
    Pair("Hard work beats talent when talent doesn't work hard.", "Tim Notke"),
    Pair("The more that you read, the more things you will know. The more that you learn, the more places you'll go.", "Dr. Seuss"),
    Pair("Your time is limited, don't waste it living someone else's life.", "Steve Jobs"),
    Pair("The only way to do great work is to love what you do.", "Steve Jobs"),
    Pair("Believe you can and you're halfway there.", "Theodore Roosevelt"),
    Pair("You are never too old to set another goal or to dream a new dream.", "C.S. Lewis")
)

/**
 * A Composable that displays a randomly selected motivational quote in a Card.
 *
 * The quote and author are styled using MaterialTheme typography and colors.
 * The card uses the primary container color from the MaterialTheme.
 *
 * @param modifier Optional [Modifier] to be applied to the Card.
 */
@Composable
fun MotivationalQuoteCard(modifier: Modifier = Modifier) {
    val randomQuote = remember { motivationalQuotes.random() }

    Card(
        modifier = modifier
            .padding(8.dp)
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
                text = "\"${randomQuote.first}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Companion.Italic,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "― ${randomQuote.second}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun WelcomeCard(userName: String) {
    val greeting = remember { getGreeting() }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userName.isNotEmpty()) "$greeting $userName!" else "$greeting!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ready for your UPSC prep journey?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = remember { DateFormatUtils.formatWelcomeDate(Date()) },
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
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