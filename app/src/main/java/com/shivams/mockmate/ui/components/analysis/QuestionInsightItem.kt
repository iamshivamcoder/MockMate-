package com.shivams.mockmate.ui.components.analysis

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
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.analysis.CognitiveTag
import com.shivams.mockmate.model.analysis.QuestionAnalysis

/**
 * List item for displaying a single question's cognitive analysis.
 */
@Composable
fun QuestionInsightItem(
    questionAnalysis: QuestionAnalysis,
    modifier: Modifier = Modifier
) {
    val (tagColor, tagIcon, tagLabel) = getTagDetails(questionAnalysis.cognitiveTag)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Question number badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tagColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Q${questionAnalysis.questionNumber}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = tagColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Tag label row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tagIcon,
                        contentDescription = tagLabel,
                        tint = tagColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tagLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = tagColor
                    )
                    
                    // Correct/Incorrect indicator
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (questionAnalysis.isCorrect) "✓ Correct" else "✗ Wrong",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (questionAnalysis.isCorrect) 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFFE53935)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Reasoning
                Text(
                    text = questionAnalysis.reasoning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Ink colors and metadata row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ink colors used
                    if (questionAnalysis.inkColorUsed.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            questionAnalysis.inkColorUsed.forEach { color ->
                                InkColorDot(inkColor = color)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                    
                    // Time spent (if available)
                    questionAnalysis.timeSpentSeconds?.let { time ->
                        Text(
                            text = "${time}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Elimination attempts
                    if (questionAnalysis.eliminationAttempts > 0) {
                        Text(
                            text = "${questionAnalysis.eliminationAttempts} eliminations",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InkColorDot(inkColor: String) {
    val color = when (inkColor.lowercase()) {
        "blue" -> Color(0xFF2196F3)
        "red" -> Color(0xFFE53935)
        "brown" -> Color(0xFF8D6E63)
        "green" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private fun getTagDetails(tag: CognitiveTag): Triple<Color, ImageVector, String> {
    return when (tag) {
        CognitiveTag.CONCEPT_COLLAPSE -> Triple(
            Color(0xFFE53935),
            Icons.Filled.BrokenImage,
            "Concept Collapse"
        )
        CognitiveTag.FLUKE -> Triple(
            Color(0xFFFFA000),
            Icons.Filled.Warning,
            "Silly Mistake"
        )
        CognitiveTag.SOLID -> Triple(
            Color(0xFF4CAF50),
            Icons.Filled.CheckCircle,
            "Solid Knowledge"
        )
        CognitiveTag.DOUBT -> Triple(
            Color(0xFF6D4C41),
            Icons.AutoMirrored.Filled.Help,
            "Doubt"
        )
        CognitiveTag.INTUITION -> Triple(
            Color(0xFF1E88E5),
            Icons.Filled.Lightbulb,
            "Intuition"
        )
    }
}
