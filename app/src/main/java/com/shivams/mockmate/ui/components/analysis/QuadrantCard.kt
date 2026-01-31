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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.Help
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.model.analysis.CognitiveTag

/**
 * Quadrant card for the 2x2 summary grid on the Analysis Dashboard.
 */
@Composable
fun QuadrantCard(
    title: String,
    count: Int,
    cognitiveTag: CognitiveTag,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, iconColor, icon) = when (cognitiveTag) {
        CognitiveTag.CONCEPT_COLLAPSE -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFEE5A5A))),
            Color.White,
            Icons.Filled.BrokenImage
        )
        CognitiveTag.FLUKE -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFFBE0B), Color(0xFFFFA000))),
            Color.White,
            Icons.Filled.Warning
        )
        CognitiveTag.SOLID -> Triple(
            Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF45A049))),
            Color.White,
            Icons.Filled.CheckCircle
        )
        CognitiveTag.DOUBT -> Triple(
            Brush.linearGradient(listOf(Color(0xFF8D6E63), Color(0xFF6D4C41))),
            Color.White,
            Icons.AutoMirrored.Filled.Help
        )
        CognitiveTag.INTUITION -> Triple(
            Brush.linearGradient(listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))),
            Color.White,
            Icons.Filled.CheckCircle
        )
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon in circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Count
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Convenience composable for the 2x2 Summary Grid layout.
 */
@Composable
fun SummaryGrid(
    conceptCollapseCount: Int,
    flukeCount: Int,
    solidCount: Int,
    doubtCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuadrantCard(
                title = "Concept Collapse",
                count = conceptCollapseCount,
                cognitiveTag = CognitiveTag.CONCEPT_COLLAPSE,
                modifier = Modifier.weight(1f)
            )
            QuadrantCard(
                title = "Silly Mistakes",
                count = flukeCount,
                cognitiveTag = CognitiveTag.FLUKE,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Bottom row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuadrantCard(
                title = "Solid Knowledge",
                count = solidCount,
                cognitiveTag = CognitiveTag.SOLID,
                modifier = Modifier.weight(1f)
            )
            QuadrantCard(
                title = "Doubt Markers",
                count = doubtCount,
                cognitiveTag = CognitiveTag.DOUBT,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
