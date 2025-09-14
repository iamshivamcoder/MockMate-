package com.shivams.mockmate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.QuestionDifficulty
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.ui.theme.extendedColorScheme

@Composable
fun UnifiedDifficultyBadge(
    text: String,
    baseColor: Color,
    accessibilityText: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    fontWeight: FontWeight? = FontWeight.Bold,
    isPulsating: Boolean = false
) {
    val animatedAlpha = if (isPulsating) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulsating_badge_transition")
        infiniteTransition.animateFloat(
            initialValue = 0.4f, // Changed from 0.7f
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing), // Changed from 800ms
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulsating_badge_alpha"
        ).value
    } else {
        0.2f // Default non-pulsating alpha
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .semantics { contentDescription = accessibilityText },
        color = baseColor.copy(alpha = if (isPulsating) animatedAlpha else 0.2f) // Use animatedAlpha for pulsating, 0.2f for static
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(contentPadding),
            style = textStyle,
            color = baseColor, // Text color remains solid
            fontWeight = fontWeight
        )
    }
}

/**
 * A badge component to display the difficulty of a test.
 *
 * @param difficulty The [TestDifficulty] to display.
 * @param isPulsating Whether the badge should have a pulsating animation.
 */
@Composable
fun DifficultyBadge(
    difficulty: TestDifficulty,
    modifier: Modifier = Modifier,
    isPulsating: Boolean = false
) {
    val color = when (difficulty) {
        TestDifficulty.EASY -> MaterialTheme.extendedColorScheme.difficultyEasyColor
        TestDifficulty.MEDIUM -> MaterialTheme.extendedColorScheme.difficultyMediumColor
        TestDifficulty.HARD -> MaterialTheme.extendedColorScheme.difficultyHardColor
    }
    UnifiedDifficultyBadge(
        text = difficulty.name.uppercase(), // Consistent casing
        baseColor = color,
        accessibilityText = "Test difficulty: ${difficulty.name}",
        modifier = modifier,
        isPulsating = isPulsating
    )
}

/**
 * A badge component to display the difficulty of a question.
 *
 * @param difficulty The [QuestionDifficulty] to display.
 * @param isPulsating Whether the badge should have a pulsating animation.
 */
@Composable
fun QuestionDifficultyBadge(
    difficulty: QuestionDifficulty,
    modifier: Modifier = Modifier,
    isPulsating: Boolean = false
) {
    val color = when (difficulty) {
        QuestionDifficulty.EASY -> MaterialTheme.extendedColorScheme.difficultyEasyColor
        QuestionDifficulty.MEDIUM -> MaterialTheme.extendedColorScheme.difficultyMediumColor
        QuestionDifficulty.HARD -> MaterialTheme.extendedColorScheme.difficultyHardColor
    }
    UnifiedDifficultyBadge(
        text = difficulty.name.uppercase(), // Consistent casing
        baseColor = color,
        accessibilityText = "Question difficulty: ${difficulty.name}",
        modifier = modifier,
        isPulsating = isPulsating
    )
}
