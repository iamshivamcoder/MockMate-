package com.shivams.mockmate.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.ui.components.MockMateTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showContent = true
    }
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(300)) + expandVertically(tween(400))
            ) {
                WelcomeHelpCard()
            }
            
            // How It Works Section
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 100)) + expandVertically(tween(500, 100))
            ) {
                HowItWorksSection()
            }
            
            // FAQ Section Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 200))
            ) {
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // FAQ Items
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, 300)) + expandVertically(tween(500, 300))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FAQItem(
                        question = "How do I import my own test data?",
                        answer = "Create a JSON file with your questions and import it via Dashboard → Import Data. See README.md for the exact format.",
                        icon = Icons.Default.Upload
                    )
                    
                    FAQItem(
                        question = "Can I take tests offline?",
                        answer = "Yes! Once tests are loaded, they're available offline. Internet is only needed for initial sync and updates.",
                        icon = Icons.Default.OfflineBolt
                    )
                    
                    FAQItem(
                        question = "How is my performance calculated?",
                        answer = "Based on correct answers, negative marking (if enabled), and time taken. You'll see subject-wise breakdowns after each test.",
                        icon = Icons.Default.Analytics
                    )
                    
                    FAQItem(
                        question = "Is there negative marking?",
                        answer = "Yes, similar to actual UPSC exams. The value (e.g., 0.33 for 1/3rd deduction) is defined in the test data.",
                        icon = Icons.Default.Quiz
                    )
                    
                    FAQItem(
                        question = "Where's the JSON format documentation?",
                        answer = "Check the README.md file in the MockMate project root directory for complete format details.",
                        icon = Icons.Default.Code
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WelcomeHelpCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
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
                        text = "Welcome to MockMate!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your personal UPSC exam prep companion. Take tests, practice, and track your progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HowItWorksSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "How It Works",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val steps = listOf(
                Triple(Icons.Default.Book, "Dashboard", "Overview of your progress and quick access"),
                Triple(Icons.Default.Quiz, "Mock Tests", "Full-length and subject-specific tests"),
                Triple(Icons.Default.Lightbulb, "Practice", "Focused practice on specific topics"),
                Triple(Icons.Default.Upload, "Import", "Add your own tests in JSON format"),
                Triple(Icons.Default.Analytics, "Analytics", "Track scores and identify weak areas"),
                Triple(Icons.Default.History, "History", "Review all past test attempts")
            )
            
            steps.forEachIndexed { index, (icon, title, desc) ->
                StepItem(
                    number = index + 1,
                    icon = icon,
                    title = title,
                    description = desc
                )
                if (index < steps.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    number: Int,
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun FAQItem(
    question: String,
    answer: String,
    icon: ImageVector
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }
        }
    }
}
