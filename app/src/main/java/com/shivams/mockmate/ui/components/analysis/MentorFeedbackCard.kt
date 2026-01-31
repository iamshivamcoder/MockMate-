package com.shivams.mockmate.ui.components.analysis

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.analysis.MentorFeedback

/**
 * Mentor Feedback Card - displays AI-generated advice with structured sections.
 * Shows key strength, critical weakness, and actionable next step.
 */
@Composable
fun MentorFeedbackCard(
    feedback: String,
    modifier: Modifier = Modifier
) {
    if (feedback.isBlank()) return
    
    // Parse structured feedback
    val mentorFeedback = MentorFeedback.fromRawFeedback(feedback)
    
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF667EEA),
                            Color(0xFF764BA2)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Header row with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bot icon in circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = "Mentor",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column {
                    Text(
                        text = "Mentor's Feedback",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "AI-powered insights",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content - Structured or Legacy
            if (mentorFeedback.isStructured) {
                StructuredFeedbackContent(mentorFeedback)
            } else {
                // Legacy plain text
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                )
            }
        }
    }
}

@Composable
private fun StructuredFeedbackContent(feedback: MentorFeedback) {
    Column {
        // Key Strength
        if (feedback.keyStrength.isNotEmpty()) {
            FeedbackSection(
                emoji = "✅",
                title = "Key Strength",
                content = feedback.keyStrength,
                contentColor = Color(0xFFB9F6CA)  // Light green
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
        
        // Critical Weakness
        if (feedback.criticalWeakness.isNotEmpty()) {
            FeedbackSection(
                emoji = "⚠️",
                title = "Critical Weakness",
                content = feedback.criticalWeakness,
                contentColor = Color(0xFFFFE0B2)  // Light orange
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
        
        // Actionable Step
        if (feedback.actionableStep.isNotEmpty()) {
            FeedbackSection(
                emoji = "🚀",
                title = "Next Step",
                content = feedback.actionableStep,
                contentColor = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}

@Composable
private fun FeedbackSection(
    emoji: String,
    title: String,
    content: String,
    contentColor: Color
) {
    Column {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Section content
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
        )
    }
}
