package com.example.mockmate.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MatchItem(val id: String, val text: String, val matchId: String)
data class MatchedPair(val itemA: MatchItem, val itemB: MatchItem) {
    val isCorrect: Boolean
        get() = itemA.matchId == itemB.id
}

// Placeholder data - replace with actual data source
val sampleItemsA = listOf(
    MatchItem("a1", "Apple", "b1"),
    MatchItem("a2", "Banana", "b2"),
    MatchItem("a3", "Cherry", "b3"),
    MatchItem("a4", "Date", "b4")
)

val sampleItemsB = listOf(
    MatchItem("b1", "A fruit that is red or green", "a1"),
    MatchItem("b2", "A long yellow fruit", "a2"),
    MatchItem("b3", "A small red fruit", "a3"),
    MatchItem("b4", "A sweet brown fruit", "a4"),
    MatchItem("b5", "A citrus fruit", "") // Extra item
).shuffled()


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTheColumnScreen(
    onNavigateBack: () -> Unit,
    testId: String? = null // Added testId parameter
) {
    var columnAItems by remember { mutableStateOf(sampleItemsA) }
    var columnBItems by remember { mutableStateOf(sampleItemsB) }

    var selectedAItemId by remember { mutableStateOf<String?>(null) }
    var selectedBItemId by remember { mutableStateOf<String?>(null) }
    val matchedPairs = remember { mutableStateListOf<MatchedPair>() }
    var submitted by remember { mutableStateOf(false) }

    // Log the testId if it's provided (placeholder for actual data loading)
    LaunchedEffect(testId) {
        if (testId != null) {
            Log.d("MatchTheColumnScreen", "Received testId: $testId")
            // Here you would typically launch a coroutine to fetch test data
            // using the testId and a ViewModel/Repository.
            // For now, it will still use sampleItemsA and sampleItemsB.
        }
    }

    val isMatched: (String) -> Boolean = { itemId ->
        matchedPairs.any { it.itemA.id == itemId || it.itemB.id == itemId }
    }

    LaunchedEffect(selectedAItemId, selectedBItemId) {
        if (selectedAItemId != null && selectedBItemId != null) {
            val itemA = columnAItems.find { it.id == selectedAItemId }
            val itemB = columnBItems.find { it.id == selectedBItemId }
            if (itemA != null && itemB != null) {
                matchedPairs.add(MatchedPair(itemA, itemB))
                // Reset selections
                selectedAItemId = null
                selectedBItemId = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // Dynamically change title based on whether it's a test or practice
                title = { Text(if (testId != null) "Match The Column Test" else "Match The Column Practice") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Column A
                ColumnItems(
                    modifier = Modifier.weight(1f),
                    items = columnAItems,
                    selectedItemId = selectedAItemId,
                    onItemClick = { itemId ->
                        if (!isMatched(itemId)) selectedAItemId = itemId
                    },
                    isMatched = isMatched,
                    submitted = submitted,
                    matchedPairs = matchedPairs,
                    isColumnA = true
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Column B
                ColumnItems(
                    modifier = Modifier.weight(1f),
                    items = columnBItems,
                    selectedItemId = selectedBItemId,
                    onItemClick = { itemId ->
                        if (!isMatched(itemId) && selectedAItemId != null) { // Only allow selection if an item in A is selected
                            selectedBItemId = itemId
                        } else if (!isMatched(itemId) && selectedAItemId == null) {
                            // Optionally allow selecting B first, or provide feedback
                        }
                    },
                    isMatched = isMatched,
                    submitted = submitted,
                    matchedPairs = matchedPairs,
                    isColumnA = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { submitted = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = matchedPairs.size == columnAItems.size && !submitted // Enable only when all A items are matched
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
fun ColumnItems(
    modifier: Modifier = Modifier,
    items: List<MatchItem>,
    selectedItemId: String?,
    onItemClick: (String) -> Unit,
    isMatched: (String) -> Boolean,
    submitted: Boolean,
    matchedPairs: List<MatchedPair>,
    isColumnA: Boolean
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            val thisItemIsMatched = isMatched(item.id)
            val pair = if (thisItemIsMatched) matchedPairs.find { it.itemA.id == item.id || it.itemB.id == item.id } else null
            val isCorrect = pair?.isCorrect

            val backgroundColor = when {
                submitted && thisItemIsMatched -> if (isCorrect == true) Color.Green.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f)
                selectedItemId == item.id -> colorScheme.primary.copy(alpha = 0.5f)
                thisItemIsMatched -> colorScheme.secondary.copy(alpha = 0.3f)
                else -> colorScheme.surfaceVariant
            }
            val textColor = when {
                submitted && thisItemIsMatched -> if (isCorrect == true) Color.DarkGray else Color.White
                selectedItemId == item.id -> colorScheme.onPrimary
                thisItemIsMatched -> colorScheme.onSecondary
                else -> colorScheme.onSurfaceVariant
            }

            MatchItemCard(
                text = item.text,
                isSelected = selectedItemId == item.id,
                isMatched = thisItemIsMatched,
                onClick = { onItemClick(item.id) },
                submitted = submitted,
                isCorrect = if (thisItemIsMatched) {
                    if (isColumnA) matchedPairs.find { it.itemA.id == item.id }?.isCorrect
                    else matchedPairs.find { it.itemB.id == item.id }?.isCorrect
                } else null,
                backgroundColor = backgroundColor,
                textColor = textColor
            )
        }
    }
}

@Composable
fun MatchItemCard(
    text: String,
    isSelected: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit,
    submitted: Boolean,
    isCorrect: Boolean?,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick, enabled = !isMatched && !submitted),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isSelected && !isMatched) BorderStroke(2.dp, colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
            if (submitted && isMatched) {
                Text(
                    text = if (isCorrect == true) "✅" else "❌",
                    fontSize = 20.sp,
                    color = if (isCorrect == true) Color.DarkGray else Color.White
                )
            }
        }
    }
}
