package com.shivams.mockmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.R
import com.shivams.mockmate.ui.theme.MockMateTheme

/**
 * Modern motivation card with image background and action button
 */
@Composable
fun MotivationCard(
    modifier: Modifier = Modifier,
    onPracticeClick: (() -> Unit)? = null
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.practice_banner),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
            
            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top text
                Column {
                    Text(
                        text = mainText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }
                
                // Bottom button
                if (onPracticeClick != null) {
                    Button(
                        onClick = onPracticeClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B35)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Pick a Test",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Load Up and Practise!",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
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
