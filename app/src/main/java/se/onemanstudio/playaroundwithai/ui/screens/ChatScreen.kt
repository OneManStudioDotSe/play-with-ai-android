package se.onemanstudio.playaroundwithai.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Clear
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
import se.onemanstudio.playaroundwithai.data.AnalysisType
import se.onemanstudio.playaroundwithai.ui.screens.views.AmoebaShapeAnimation
import se.onemanstudio.playaroundwithai.ui.screens.views.AnalysisHeader
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

    // --- UPDATED STATE MANAGEMENT ---
    // 1. Revert to a single nullable Uri
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    // 2. State for the dropdown (unchanged)
    var analysisType by remember { mutableStateOf(AnalysisType.PRODUCT) }

    // 3. Revert launcher to single-image picker
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
                            viewModel.generateContent(prompt.text, selectedImageUri, analysisType)
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
            Column {
                AnalysisHeader(
                    selectedImageUri = selectedImageUri,
                    analysisType = analysisType,
                    onAnalysisTypeChange = { analysisType = it },
                    onClearImage = { selectedImageUri = null }
                )

                PromptInputSection(
                    textState = textState,
                    onTextChanged = { textState = it },
                    onSendClicked = {
                        viewModel.generateContent(textState.text, selectedImageUri, analysisType)
                        keyboardController?.hide()
                        textState = TextFieldValue("")
                        selectedImageUri = null
                    },
                    onChipClicked = { prompt -> textState = TextFieldValue(prompt) },
                    onClearClicked = { textState = TextFieldValue("") },
                    onAttachClicked = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center // Center content by default
        ) {
            when (val state = uiState) {
                is ChatUiState.Initial -> {
                    // When the state is initial, the Amoeba is centered perfectly by the parent Box.
                    AmoebaShapeAnimation()
                }

                is ChatUiState.Loading -> {
                    // The loading indicator is also centered perfectly.
                    CircularProgressIndicator()
                }

                else -> {
                    // For Success or Error, we use the original scrollable Column.
                    // This Column now lives inside the Box but fills it.
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .weight(1f),
                            contentAlignment = Alignment.TopStart // Align text to the top-start
                        ) {
                            if (state is ChatUiState.Success) {
                                TypewriterText(text = state.outputText)
                                IconButton(
                                    onClick = { viewModel.clearResponse() },
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Icon(Icons.Default.Clear, "Clear response")
                                }
                            } else if (state is ChatUiState.Error) {
                                Text(
                                    text = state.errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                IconButton(
                                    onClick = { viewModel.clearResponse() },
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Icon(Icons.Default.Clear, "Clear response")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


