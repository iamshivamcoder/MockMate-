package com.example.mockmate.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MotivationalQuoteCard(modifier: Modifier = Modifier) {
    val quotes = listOf(
        Pair("Success is not final, failure is not fatal: It is the courage to continue that counts.", "Winston Churchill"),
        Pair("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt"),
        Pair("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
        Pair("Everything you've ever wanted is on the other side of fear.", "George Addair"),
        Pair("Hard work beats talent when talent doesn't work hard.", "Tim Notke"),
        Pair("The more that you read, the more things you will know. The more that you learn, the more places you'll go.", "Dr. Seuss"),
        Pair("Your time is limited, don't waste it living someone else's life.", "Steve Jobs"),
        Pair("The only way to do great work is to love what you do.", "Steve Jobs"),
        Pair("Believe you can and you're halfway there.", "Theodore Roosevelt"),
        Pair("You are never too old to set another goal or to dream a new dream.", "C.S. Lewis")
    )
    
    val randomQuote = remember { quotes.random() }
    
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\"${randomQuote.first}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "― ${randomQuote.second}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}