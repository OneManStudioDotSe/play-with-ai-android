package se.onemanstudio.playaroundwithai.feature.chat.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import se.onemanstudio.playaroundwithai.core.data.AnalysisType
import se.onemanstudio.playaroundwithai.core.data.InputMode
import se.onemanstudio.playaroundwithai.core.ui.views.HistoryItemCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.views.AmoebaShapeAnimation
import se.onemanstudio.playaroundwithai.core.ui.views.AnalysisHeader
import se.onemanstudio.playaroundwithai.core.ui.views.FilePreviewHeader
import se.onemanstudio.playaroundwithai.core.ui.views.PromptInputSection
import se.onemanstudio.playaroundwithai.feature.chat.Attachment
import se.onemanstudio.playaroundwithai.feature.chat.ChatUiState
import se.onemanstudio.playaroundwithai.feature.chat.ChatViewModel

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
            onDismissRequest = { viewModel.closeHistorySheet() },
            shape = RoundedCornerShape(Dimensions.paddingMedium),
            containerColor = Color.Transparent,
        ) {
            // Our own custom-styled sheet content
            NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.paddingLarge),
                    contentPadding = PaddingValues(top = Dimensions.paddingLarge, bottom = Dimensions.paddingExtraLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)
                ) {
                    item {
                        Text(
                            text = "Prompt history",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = Dimensions.paddingMedium)
                        )
                    }
                    items(history) { prompt ->
                        // Use our new HistoryItemCard
                        HistoryItemCard(
                            prompt = prompt,
                            onClick = { selectedText ->
                                textState = TextFieldValue(selectedText)
                                viewModel.closeHistorySheet()
                                keyboardController?.hide()
                            }
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        // Use the background color from our theme
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoBrutalTopAppBar(
                title = "Let's talk",
                actions = {
                    NeoBrutalIconButton(
                        onClick = { viewModel.openHistorySheet() },
                        imageVector = Icons.Default.History,
                        contentDescription = "Prompt History",
                        backgroundColor = MaterialTheme.colorScheme.tertiary // Use an accent color
                    )
                }
            )
        },
        bottomBar = {
            NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(top = Dimensions.paddingMedium)) {
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
                is ChatUiState.Loading -> CircularProgressIndicator(
                    // Style the indicator
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = Dimensions.paddingSmall
                )

                else -> {
                    // Display the result in a NeoBrutalCard
                    NeoBrutalCard(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimensions.paddingLarge)
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(Dimensions.paddingLarge)
                        ) {
                            if (state is ChatUiState.Success) {
                                //TypewriterText(text = state.outputText)
                                Text(
                                    text = state.outputText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                NeoBrutalIconButton(
                                    onClick = { viewModel.clearResponse() },
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear response"
                                )
                            } else if (state is ChatUiState.Error) {
                                NeoBrutalCard(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(Dimensions.paddingLarge),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(Dimensions.paddingLarge),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = state.errorMessage,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                        NeoBrutalIconButton(
                                            onClick = { viewModel.clearResponse() },
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear response"
                                        )
                                    }
                                }
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
