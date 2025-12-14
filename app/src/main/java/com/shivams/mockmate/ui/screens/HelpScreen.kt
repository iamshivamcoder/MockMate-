package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.ui.components.ExpandableContentCard
import com.shivams.mockmate.ui.components.MockMateTopBar
import com.shivams.mockmate.ui.components.rememberExpandableCardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            MockMateTopBar(
                title = "Help & FAQ",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showSettings = false
            )
        }
    ) { paddingValues ->
        val topGradientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        val bottomGradientColor = MaterialTheme.colorScheme.background

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(topGradientColor, bottomGradientColor)
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            HelpSection(
                icon = Icons.Filled.Info,
                title = "What is MockMate?",
                content = buildAnnotatedString {
                    append("MockMate is your personal companion for UPSC exam preparation. It allows you to take mock tests, practice specific subjects, import your own tests, and analyze your performance to identify areas for improvement.")
                }
            )

            HelpSection(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                title = "How does this app work?",
                content = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("1. Dashboard: ")
                    }
                    append("Get an overview of your progress and quick access to features.\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("2. Mock Tests: ")
                    }
                    append("Choose from pre-defined full-length or subject-specific tests.\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("3. Practice: ")
                    }
                    append("Focus on specific subjects or topics with customized practice sessions.\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("4. Import Tests: ")
                    }
                    append("Create your own tests in JSON format and import them into the app. (See README.md for format details)\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("5. Performance Analysis: ")
                    }
                    append("After each test, review your answers, see detailed explanations, and track your scores over time.\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("6. Test History: ")
                    }
                    append("Access all your past attempts and review your performance trends.")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Frequently Asked Questions (FAQ)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            val importOwnTestState = rememberExpandableCardState(key = "importOwnTest")
            ExpandableContentCard(
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "How do I import my own test data?",
                initiallyExpanded = importOwnTestState.value,
                onExpandedChange = { importOwnTestState.value = it }
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("You can import tests by creating a JSON file with your questions, options, correct answers, and other details. The specific format is described in the README.md file of this project. From the Dashboard, tap 'Import Data' and select your JSON file.")
                }
            }

            val offlineTestState = rememberExpandableCardState(key = "offlineTest")
            ExpandableContentCard(
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "Can I take tests offline?",
                initiallyExpanded = offlineTestState.value,
                onExpandedChange = { offlineTestState.value = it }
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("Yes, once tests are loaded into the app (either pre-defined or imported), you can take them offline. An internet connection is primarily needed for initial data sync, updates, or if future cloud features are implemented.")
                }
            }

            val performanceCalculationState = rememberExpandableCardState(key = "performanceCalculation")
            ExpandableContentCard(
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "How is my performance calculated?",
                initiallyExpanded = performanceCalculationState.value,
                onExpandedChange = { performanceCalculationState.value = it }
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("Performance is calculated based on the number of correct answers, incorrect answers (considering negative marking if enabled), and the time taken. You\'ll see a detailed breakdown after each test, including subject-wise scores.")
                }
            }

            val negativeMarkingState = rememberExpandableCardState(key = "negativeMarking")
            ExpandableContentCard(
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "Is there negative marking?",
                initiallyExpanded = negativeMarkingState.value,
                onExpandedChange = { negativeMarkingState.value = it }
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("Yes, mock tests can have negative marking, similar to the actual UPSC exams. The value for negative marking (e.g., 0.33 for 1/3rd deduction) is defined within the test data.")
                }
            }

            val jsonFormatState = rememberExpandableCardState(key = "jsonFormat")
            ExpandableContentCard(
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "Where can I find the JSON format for importing tests?",
                initiallyExpanded = jsonFormatState.value,
                onExpandedChange = { jsonFormatState.value = it }
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("The JSON format details are available in the README.md file located in the root directory of the MockMate project.")
                }
            }

            val contributionState = rememberExpandableCardState(key = "contribution")
            ExpandableContentCard(
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "How can I contribute to MockMate development?",
                initiallyExpanded = contributionState.value,
                onExpandedChange = { contributionState.value = it }
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("Please refer to the 'Project Vision' section in the README.md file. For specific contribution guidelines (branching, PRs, etc.), please consult with the project maintainers.")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HelpSection(icon: ImageVector, title: String, content: AnnotatedString) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
