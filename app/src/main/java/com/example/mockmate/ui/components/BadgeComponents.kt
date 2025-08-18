package com.example.mockmate.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mockmate.model.QuestionDifficulty
import com.example.mockmate.model.TestDifficulty
import com.example.mockmate.ui.theme.extendedColorScheme

/**
 * A badge component to display the difficulty of a test.
 *
 * @param difficulty The [TestDifficulty] to display.
 */
@Composable
fun DifficultyBadge(difficulty: TestDifficulty) {
    val color = when (difficulty) {
        TestDifficulty.EASY -> MaterialTheme.extendedColorScheme.difficultyEasyColor
        TestDifficulty.MEDIUM -> MaterialTheme.extendedColorScheme.difficultyMediumColor
        TestDifficulty.HARD -> MaterialTheme.extendedColorScheme.difficultyHardColor
    }
    
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = difficulty.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * A badge component to display the difficulty of a question.
 *
 * @param difficulty The [QuestionDifficulty] to display.
 */
@Composable
fun QuestionDifficultyBadge(difficulty: QuestionDifficulty) {
    val color = when (difficulty) {
        QuestionDifficulty.EASY -> MaterialTheme.extendedColorScheme.difficultyEasyColor
        QuestionDifficulty.MEDIUM -> MaterialTheme.extendedColorScheme.difficultyMediumColor
        QuestionDifficulty.HARD -> MaterialTheme.extendedColorScheme.difficultyHardColor
    }
    
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = difficulty.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
