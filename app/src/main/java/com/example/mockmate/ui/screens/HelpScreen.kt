package com.example.mockmate.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Info 
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mockmate.ui.components.MockMateTopBar

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
            Spacer(modifier = Modifier.height(16.dp))

            HelpSection(
                icon = Icons.Filled.Info,
                title = "What is MockMate?",
                content = "MockMate is your personal companion for UPSC exam preparation. It allows you to take mock tests, practice specific subjects, import your own tests, and analyze your performance to identify areas for improvement."
            )

            HelpSection(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                title = "How does this app work?",
                content = """
                    1.  **Dashboard:** Get an overview of your progress and quick access to features.
                    2.  **Mock Tests:** Choose from pre-defined full-length or subject-specific tests.
                    3.  **Practice:** Focus on specific subjects or topics with customized practice sessions.
                    4.  **Import Tests:** Create your own tests in JSON format and import them into the app. (See README.md for format details)
                    5.  **Performance Analysis:** After each test, review your answers, see detailed explanations, and track your scores over time.
                    6.  **Test History:** Access all your past attempts and review your performance trends.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Frequently Asked Questions (FAQ)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FAQItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                question = "How do I import my own test data?",
                answer = "You can import tests by creating a JSON file with your questions, options, correct answers, and other details. The specific format is described in the README.md file of this project. From the Dashboard, tap 'Import Data' and select your JSON file."
            )

            FAQItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                question = "Can I take tests offline?",
                answer = "Yes, once tests are loaded into the app (either pre-defined or imported), you can take them offline. An internet connection is primarily needed for initial data sync, updates, or if future cloud features are implemented."
            )

            FAQItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                question = "How is my performance calculated?",
                answer = "Performance is calculated based on the number of correct answers, incorrect answers (considering negative marking if enabled), and the time taken. You'll see a detailed breakdown after each test, including subject-wise scores."
            )

            FAQItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                question = "Is there negative marking?",
                answer = "Yes, mock tests can have negative marking, similar to the actual UPSC exams. The value for negative marking (e.g., 0.33 for 1/3rd deduction) is defined within the test data."
            )
            
            FAQItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                question = "Where can I find the JSON format for importing tests?",
                answer = "The JSON format details are available in the README.md file located in the root directory of the MockMate project."
            )

             FAQItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                question = "How can I contribute to MockMate development?",
                answer = "Please refer to the 'Project Vision' section in the README.md file. For specific contribution guidelines (branching, PRs, etc.), please consult with the project maintainers."
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HelpSection(icon: ImageVector, title: String, content: String) {
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FAQItem(icon: ImageVector, question: String, answer: String) {
    var isExpanded by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = question, // Content description for icon
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = question, // Removed "Q:" prefix
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f) // Allow question to take available space
                )
                Spacer(modifier = Modifier.width(8.dp)) // Space before expand icon
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column { // Wrap answer in a column for proper spacing if needed in future
                    Spacer(modifier = Modifier.height(8.dp)) // Adjusted spacer
                    Text(
                        text = answer, // Removed "A:" prefix
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
