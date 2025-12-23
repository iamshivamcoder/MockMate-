package com.shivams.mockmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivams.mockmate.ui.components.ChatInputField
import com.shivams.mockmate.ui.components.ChatMessageItem
import com.shivams.mockmate.ui.components.ContextSuggestionChips
import com.shivams.mockmate.ui.components.QuickActionChips
import com.shivams.mockmate.ui.components.TypingIndicator
import com.shivams.mockmate.ui.viewmodels.MentorChatViewModel

/**
 * Mentor Chat Screen - AI-powered chatbot with UPSC mentor persona
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorChatScreen(
    viewModel: MentorChatViewModel,
    onNavigateBack: () -> Unit,
    profileViewModel: com.shivams.mockmate.ui.viewmodels.ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Show error in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Mentor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your UPSC preparation guide",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChatHistory() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear chat"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column {
                // Context-aware suggestions above input (Perplexity-style)
                if (uiState.messages.size > 2 && !uiState.isTyping && uiState.contextSuggestions.isNotEmpty()) {
                    ContextSuggestionChips(
                        suggestions = uiState.contextSuggestions,
                        onSuggestionClick = { viewModel.sendMessage(it) }
                    )
                }
                
                // Quick actions when chat has few messages
                if (uiState.messages.size <= 2 && !uiState.isTyping) {
                    QuickActionChips(
                        actions = viewModel.quickActions,
                        onActionClick = { viewModel.sendQuickAction(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                ChatInputField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    onSend = { viewModel.sendMessage() },
                    enabled = !uiState.isTyping
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.messages.isEmpty() && !uiState.isTyping) {
                // Empty state - will show greeting after init
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Starting chat...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        ChatMessageItem(
                            message = message,
                            userAvatar = userProfile?.avatar,
                            onSuggestionClick = { suggestion ->
                                viewModel.sendMessage(suggestion)
                            }
                        )
                    }
                    
                    // Typing indicator
                    if (uiState.isTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                    
                    // Spacer at bottom for better scrolling
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Compact chat launcher button for embedding in other screens
 */
@Composable
fun ChatLauncherButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text("💬 Ask AI Mentor")
    }
}
