package se.onemanstudio.playaroundwithai.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import se.onemanstudio.playaroundwithai.ui.screens.views.PromptInputSection
import se.onemanstudio.playaroundwithai.ui.screens.views.TypewriterText
import se.onemanstudio.playaroundwithai.viewmodels.ChatUiState
import se.onemanstudio.playaroundwithai.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class) // Needed for TopAppBar
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current // 1. Get the keyboard controller

    val isSheetOpen by viewModel.isSheetOpen.collectAsState()
    val history by viewModel.promptHistory.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

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
                            textState = TextFieldValue(prompt.text)
                            viewModel.generateContent(prompt.text, null)
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
                    viewModel.generateContent(textState.text, selectedImageUri)
                    keyboardController?.hide()
                    // Clear inputs after sending
                    textState = TextFieldValue("")
                    selectedImageUri = null
                },
                onChipClicked = { prompt -> textState = TextFieldValue(prompt) },
                onClearClicked = { textState = TextFieldValue("") },
                // Pass new state and lambdas for image handling
                selectedImageUri = selectedImageUri,
                onClearImage = { selectedImageUri = null },
                onAttachClicked = {
                    // Launch the photo picker
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
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


