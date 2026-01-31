package com.shivams.mockmate.ui.components.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
 * Redesigned list item for displaying a single question's cognitive analysis.
 * Features:
 * - ElevatedCard with shadow (no border)
 * - Colored vertical strip on left edge
 * - Properly aligned icons
 */
@Composable
fun QuestionInsightItem(
    questionAnalysis: QuestionAnalysis,
    modifier: Modifier = Modifier
) {
    val (tagColor, tagIcon, tagLabel) = getTagDetails(questionAnalysis.cognitiveTag)
    
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Required for fillMaxHeight on child
        ) {
            // Colored vertical strip indicator
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(tagColor)
            )
            
            // Main content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Question number badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(tagColor.copy(alpha = 0.12f)),
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
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Tag label row with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = tagIcon,
                            contentDescription = tagLabel,
                            tint = tagColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tagLabel,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = tagColor
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Correct/Incorrect badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (questionAnalysis.isCorrect)
                                        Color(0xFF4CAF50).copy(alpha = 0.12f)
                                    else
                                        Color(0xFFE53935).copy(alpha = 0.12f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (questionAnalysis.isCorrect) "✓ Correct" else "✗ Wrong",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (questionAnalysis.isCorrect)
                                    Color(0xFF2E7D32)
                                else
                                    Color(0xFFC62828)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Reasoning text
                    Text(
                        text = questionAnalysis.reasoning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.1f
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Metadata row - ink colors and stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ink colors used
                        if (questionAnalysis.inkColorUsed.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                questionAnalysis.inkColorUsed.forEach { color ->
                                    InkColorDot(inkColor = color)
                                }
                            }
                        }
                        
                        // Time spent
                        questionAnalysis.timeSpentSeconds?.let { time ->
                            MetadataChip(text = "${time}s")
                        }
                        
                        // Elimination attempts
                        if (questionAnalysis.eliminationAttempts > 0) {
                            MetadataChip(text = "${questionAnalysis.eliminationAttempts} elim.")
                        }
                        
                        // Strikethroughs
                        if (questionAnalysis.strikethroughCount > 0) {
                            MetadataChip(text = "${questionAnalysis.strikethroughCount} strikes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    )
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
            .size(14.dp)
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
            Color(0xFF43A047),
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
