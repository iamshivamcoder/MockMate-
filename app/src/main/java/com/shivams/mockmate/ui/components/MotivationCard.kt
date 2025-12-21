package com.shivams.mockmate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.ui.theme.MockMateTheme

/**
 * Modern motivation card with gradient background and animated icon
 */
@Composable
fun MotivationCard(
    modifier: Modifier = Modifier
) {
    // Dynamic motivational statements with regional flavours
    val practiceMessages = listOf(
        // Mumbai (Bambaiyya)
        "Chal bhidu, thoda dimaag ki batti jala!",
        "Apun ready hai, tu bhi ho ja!",
        "Practice ka kya bolta hai? Kar daal!",
        // Haryanavi
        "Re laadle, do-chaar sawal maar le!",
        "Kati tayaar hai practice ke liye?",
        "Chal, dekhe kitna dum hai!",
        // Delhi
        "Oye! Practice scene set karein?",
        "Bhai, full power prep ho jaye?",
        "Chal na, do-teen mock test niptaate hain.",
        // Bhojpuri
        "Ka ho, practice kare ke mann ba?",
        "Chala, aaj garda udaa diyal jaa!"
    )

    val subMessages = listOf(
        // Mumbai
        "Load nahi lene ka, bas practice karne ka.",
        "Ek number banega, likh ke le.",
        "Shaane, yahi time hai padhne ka!",
        // Haryanavi
        "Officer banna hai toh ragda laagega.",
        "Tora baith jaayega, laage reh!",
        "Maa-baapu ka naam roshan karna hai!",
        // Delhi
        "IAS-VAIAS ban, scene sorted hai.",
        "Bhai, chill maarke padh, ho jaayega.",
        "System set karna hai poora!",
        // Bhojpuri
        "Ehi se badka officer banba.",
        "Tohaar sapna poora hokhi!"
    )

    val mainText = remember { practiceMessages.random() }
    val subText = remember { subMessages.random() }
    
    // Pulsing animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFFF8E53)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mainText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .rotate(rotation),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MotivationCardPreview() {
    MockMateTheme {
        MotivationCard()
    }
}
