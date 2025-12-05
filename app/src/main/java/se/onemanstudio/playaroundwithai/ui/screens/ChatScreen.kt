package se.onemanstudio.playaroundwithai.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.data.AnalysisType
import se.onemanstudio.playaroundwithai.core.data.InputMode
import se.onemanstudio.playaroundwithai.ui.screens.views.AmoebaShapeAnimation
import se.onemanstudio.playaroundwithai.ui.screens.views.AnalysisHeader
import se.onemanstudio.playaroundwithai.ui.screens.views.FilePreviewHeader
import se.onemanstudio.playaroundwithai.ui.screens.views.PromptInputSection
import se.onemanstudio.playaroundwithai.viewmodels.Attachment
import se.onemanstudio.playaroundwithai.viewmodels.ChatUiState
import se.onemanstudio.playaroundwithai.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isSheetOpen by viewModel.isSheetOpen.collectAsState()
    val history by viewModel.promptHistory.collectAsState()

    var inputMode by remember { mutableStateOf(InputMode.TEXT) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var analysisType by remember { mutableStateOf(AnalysisType.PRODUCT) }

    val context = LocalContext.current
    val selectedFileName = remember(selectedFileUri) { selectedFileUri?.let { getFileName(context, it) } }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedFileUri = uri }
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
            TopAppBar(
                title = { Text("Let's talk") },
                actions = {
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
                when (inputMode) {
                    InputMode.IMAGE -> AnalysisHeader(
                        selectedImageUri = selectedImageUri,
                        analysisType = analysisType,
                        onAnalysisTypeChange = { analysisType = it },
                        onClearImage = { selectedImageUri = null }
                    )

                    InputMode.DOCUMENT -> FilePreviewHeader(
                        fileName = selectedFileName,
                        onClearFile = { selectedFileUri = null }
                    )

                    InputMode.TEXT -> {}
                }

                PromptInputSection(
                    textState = textState,
                    inputMode = inputMode,
                    onModeChange = { newMode ->
                        inputMode = newMode
                        selectedImageUri = null
                        selectedFileUri = null
                    },
                    onTextChanged = { textState = it },
                    onChipClicked = { prompt -> textState = TextFieldValue(prompt) },
                    onClearClicked = { textState = TextFieldValue("") },
                    onAttachClicked = {
                        when (inputMode) {
                            InputMode.IMAGE -> imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )

                            InputMode.DOCUMENT -> documentPickerLauncher.launch(arrayOf("*/*"))
                            InputMode.TEXT -> {}
                        }
                    },
                    onSendClicked = {
                        val attachment = when (inputMode) {
                            InputMode.IMAGE -> selectedImageUri?.let { Attachment.Image(it, analysisType) }
                            InputMode.DOCUMENT -> selectedFileUri?.let { Attachment.Document(it) }
                            InputMode.TEXT -> null
                        }
                        viewModel.generateContent(textState.text, attachment)
                        keyboardController?.hide()
                        textState = TextFieldValue("")
                        selectedImageUri = null
                        selectedFileUri = null
                    },
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ChatUiState.Initial -> AmoebaShapeAnimation()
                is ChatUiState.Loading -> CircularProgressIndicator()

                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            if (state is ChatUiState.Success) {
                                //TypewriterText(text = state.outputText)
                                Text(text = state.outputText)
                                IconButton(
                                    onClick = { viewModel.clearResponse() },
                                ) { Icon(Icons.Default.Clear, "Clear response") }
                            } else if (state is ChatUiState.Error) {
                                Text(
                                    text = state.errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                IconButton(
                                    onClick = { viewModel.clearResponse() },
                                ) { Icon(Icons.Default.Clear, "Clear response") }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    // Use a content resolver to query the file name from the URI
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    return fileName
}
