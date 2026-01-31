package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.model.TestDifficulty
import com.shivams.mockmate.ui.viewmodels.TrueFalseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseSelectionScreen(
    viewModel: TrueFalseViewModel,
    onNavigateBack: () -> Unit,
    onSessionStarted: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("By Topic", "From Text")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("True or False") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Train Your UPSC Brain", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Swipe Right → True | Swipe Left → False | Swipe Up → Skip\n\nDetect hidden qualifiers, scope distortions, and half-truths that UPSC loves to test.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (selectedTab == 0) {
                // Topic Mode UI
                OutlinedTextField(
                    value = uiState.topic,
                    onValueChange = { viewModel.updateTopic(it) },
                    label = { Text("Topic") },
                    placeholder = { Text("e.g., Indian Polity, Mughal Empire") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.topicError != null,
                    supportingText = { uiState.topicError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                var subjectExpanded by remember { mutableStateOf(false) }
                val subjects = listOf("History", "Geography", "Polity", "Economy", "Science & Tech", "Environment", "Art & Culture")
                
                ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.subject, onValueChange = {}, readOnly = true, label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(text = { Text(subject) }, onClick = { viewModel.updateSubject(subject); subjectExpanded = false })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                var difficultyExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = difficultyExpanded, onExpandedChange = { difficultyExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.difficulty.name, onValueChange = {}, readOnly = true, label = { Text("Difficulty") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = difficultyExpanded, onDismissRequest = { difficultyExpanded = false }) {
                        TestDifficulty.entries.forEach { difficulty ->
                            DropdownMenuItem(text = { Text(difficulty.name) }, onClick = { viewModel.updateDifficulty(difficulty); difficultyExpanded = false })
                        }
                    }
                }
            } else {
                // Text Import Mode UI
                OutlinedTextField(
                    value = uiState.sourceText,
                    onValueChange = { viewModel.updateSourceText(it) },
                    label = { Text("Paste Text Content") },
                    placeholder = { Text("Paste notes, articles, or paragraphs here. AI will generate True/False statements from this text.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp), // Taller for text area
                    isError = uiState.sourceTextError != null,
                    supportingText = { uiState.sourceTextError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    maxLines = 15
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Number of Statements: ${uiState.numberOfStatements}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
            Slider(value = uiState.numberOfStatements.toFloat(), onValueChange = { viewModel.updateNumberOfStatements(it.toInt()) }, valueRange = 5f..25f, steps = 3, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Negative Marking", style = MaterialTheme.typography.titleSmall)
                    Text(if (uiState.negativeMarking) "-${uiState.negativeMarkingValue} per wrong answer" else "Disabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = uiState.negativeMarking, onCheckedChange = { viewModel.updateNegativeMarking(it) })
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            uiState.generationError?.let { error ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = { 
                    if (selectedTab == 0) {
                        viewModel.generateStatements { sessionId -> onSessionStarted(sessionId) }
                    } else {
                        viewModel.generateStatementsFromText { sessionId -> onSessionStarted(sessionId) }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Generating Statements...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedTab == 0) "Generate from Topic" else "Generate from Text", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("💡 Tip: ${if (selectedTab == 0) "Start with static subjects (Polity, History) for clearer distinctions." else "Paste high-quality text for the best questions."}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
