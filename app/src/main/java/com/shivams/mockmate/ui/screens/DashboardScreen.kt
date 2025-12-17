package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DataExploration
// import androidx.compose.material.icons.filled.Pets // Import for placeholder icon - No longer needed here
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
// import androidx.compose.material3.TextButton // No longer needed here
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.model.UserStats
import com.shivams.mockmate.ui.components.ActionButton
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.PrelimsCountdownCard
import com.shivams.mockmate.ui.components.WelcomeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onPracticeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onStreakClick: () -> Unit,
    onSavedQuestionsClick: () -> Unit,
    repository: TestRepository
) {
    val userStats by repository.userStats.collectAsState(initial = UserStats(questionsAnswered = 0, correctAnswers = 0, currentStreak = 0, longestStreak = 0))
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MockMateTopBar(
                title = "MockMate",
                showBackButton = false,
                showSettings = false,
                currentStreak = userStats.currentStreak,
                onSettingsClick = onSettingsClick,
                onStreakClick = onStreakClick,
                onImportClick = onImportClick, // Pass the onImportClick lambda - forcing recompile
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply innerPadding from Scaffold
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WelcomeCard(userName = "")

            PracticeLureSection()

            PrelimsCountdownCard()

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "Start Practicing",
                icon = Icons.Default.PlayArrow,
                onClick = onPracticeClick,
                primaryColor = MaterialTheme.colorScheme.primary
            )

            ActionButton(
                text = "View History", // Changed text to be more specific
                icon = Icons.Default.DataExploration,
                onClick = onHistoryClick,
                primaryColor = MaterialTheme.colorScheme.secondary
            )

            ActionButton(
                text = "View Analytics", // New button for Analytics
                icon = Icons.Default.Analytics, // Using a new icon
                onClick = onAnalyticsClick,
                primaryColor = MaterialTheme.colorScheme.tertiary // Example color, adjust as needed
            )

            ActionButton(
                text = "Import Data",
                icon = Icons.Default.Upload,
                onClick = onImportClick,
                primaryColor = MaterialTheme.colorScheme.tertiary
            )

            ActionButton(
                text = "Saved Questions",
                icon = Icons.Default.Bookmark,
                onClick = onSavedQuestionsClick,
                primaryColor = MaterialTheme.colorScheme.secondary
            )
        }
    }

}

@Composable
fun PracticeLureSection() {

    // Step 1: List of dynamic motivational statements (Now with regional flavours!)
    val practiceMessages = listOf(
        // --- Mumbai (Bambaiyya) ---
        "Chal bhidu, thoda dimaag ki batti jala!",
        "Apun ready hai, tu bhi ho ja!",
        "Practice ka kya bolta hai? Kar daal!",

        // --- Haryanavi ---
        "Re laadle, do-chaar sawal maar le!",
        "Kati tayaar hai practice ke liye?",
        "Chal, dekhe kitna dum hai!",

        // --- Delhi ---
        "Oye! Practice scene set karein?",
        "Bhai, full power prep ho jaye?",
        "Chal na, do-teen mock test hi niptaate hain.",

        // --- Bhojpuri / Purvanchali ---
        "Ka ho, practice kare ke mann ba?",
        "Chala, aaj garda udaa diyal jaa!"
    )

    val subMessages = listOf(
        // --- Mumbai (Bambaiyya) ---
        "Load nahi lene ka, bas practice karne ka.",
        "Ek number banega, likh ke le.",
        "Shaane, yahi time hai padhne ka!",

        // --- Haryanavi ---
        "Officer banna hai toh ragda laagega.",
        "Tora baith jaayega, laage reh!",
        "Maa-baapu ka naam roshan karna hai!",

        // --- Delhi ---
        "IAS-VAIAS ban, scene sorted hai.",
        "Bhai, chill maarke padh, ho jaayega.",
        "System set karna hai poora!",

        // --- Bhojpuri / Purvanchali ---
        "Ehi se badka officer banba.",
        "Tohaar sapna poora hokhi!",
        "Bilkul fikar mat kara, sab ho jayi."
    )

    // Step 2: Pick random statements (only once when Composable first loads)
    val mainText = remember { practiceMessages.random() }
    val subText = remember { subMessages.random() }

    // Step 3: UI layout same as before
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = "Practice Rocket",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
