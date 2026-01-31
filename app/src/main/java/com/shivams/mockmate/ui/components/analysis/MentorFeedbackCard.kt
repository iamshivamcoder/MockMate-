package com.shivams.mockmate.ui.components.analysis

import androidx.compose.foundation.background
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

/**
 * Mentor Feedback Card - displays AI-generated advice and insights.
 * Features a distinct purple/blue gradient background with a bot icon.
 */
@Composable
fun MentorFeedbackCard(
    feedback: String,
    modifier: Modifier = Modifier
) {
    if (feedback.isBlank()) return
    
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
                androidx.compose.foundation.layout.Box(
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
            
            // Feedback content
            Text(
                text = feedback,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
            )
        }
    }
}
