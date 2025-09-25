package se.onemanstudio.playaroundwithai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.viewmodels.ChatUiState
import se.onemanstudio.playaroundwithai.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class) // Needed for TopAppBar
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current // 1. Get the keyboard controller

    // Get new state from ViewModel
    val isSheetOpen by viewModel.isSheetOpen.collectAsState()
    val history by viewModel.promptHistory.collectAsState()

    // --- Bottom Sheet Logic ---
    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeHistorySheet() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Text(
                        "Prompt History",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(history) { prompt ->
                    ListItem(
                        headlineContent = { Text(prompt.text) },
                        modifier = Modifier.clickable {
                            // On click: update text, send request, and close sheet
                            textState = TextFieldValue(prompt.text)
                            viewModel.generateContent(prompt.text)
                            viewModel.closeHistorySheet()
                            keyboardController?.hide()
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            // Request 1: Add a top bar
            TopAppBar(
                title = { Text("Let's talk") },
                actions = {
                    // Add history icon button
                    IconButton(onClick = { viewModel.openHistorySheet() }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Prompt History",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            PromptInputSection(
                textState = textState,
                onTextChanged = { textState = it },
                onSendClicked = {
                    viewModel.generateContent(textState.text)
                    keyboardController?.hide() // 2. Hide keyboard on send
                },
                onChipClicked = { prompt ->
                    // Request 2: Update text field on chip click, but don't send
                    textState = TextFieldValue(prompt)
                    keyboardController?.hide() // 2. Hide keyboard on send
                },
                onClearClicked = {
                    // Request 2: Clear the text field
                    textState = TextFieldValue("")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is ChatUiState.Initial -> {
                    Text("Welcome! Ask me anything.", style = MaterialTheme.typography.bodyLarge)
                }

                is ChatUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is ChatUiState.Success -> {
                    // Request 4: We'll implement the typewriter effect here
                    TypewriterText(text = state.outputText)
                }

                is ChatUiState.Error -> {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun PromptInputSection(
    textState: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onSendClicked: () -> Unit,
    onChipClicked: (String) -> Unit,
    onClearClicked: () -> Unit // New parameter
) {
    val samplePrompts = listOf("Explain Quantum Computing", "Recipe for a cake", "Write a poem about rain")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // <-- This is the line you need to add
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(samplePrompts) { prompt ->
                SuggestionChip(
                    onClick = { onChipClicked(prompt) },
                    label = { Text(prompt) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textState,
                onValueChange = onTextChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Enter your prompt") },
                // Request 2: Add the clear icon
                trailingIcon = {
                    if (textState.text.isNotEmpty()) {
                        IconButton(onClick = onClearClicked) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear text"
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSendClicked, enabled = textState.text.isNotBlank()) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
