package se.onemanstudio.playaroundwithai.feature.chat

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import se.onemanstudio.playaroundwithai.core.data.AnalysisType
import se.onemanstudio.playaroundwithai.core.data.InputMode
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.views.AmoebaShapeAnimation
import se.onemanstudio.playaroundwithai.feature.chat.models.Attachment
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatError
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatUiState
import se.onemanstudio.playaroundwithai.feature.chat.views.ChatInputArea
import se.onemanstudio.playaroundwithai.feature.chat.views.HistoryBottomSheet

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
        HistoryBottomSheet(
            history = history,
            onDismissRequest = { viewModel.closeHistorySheet() },
            onHistoryItemClick = { selectedText ->
                textState = TextFieldValue(selectedText)
                viewModel.closeHistorySheet()
                keyboardController?.hide()
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoBrutalTopAppBar(
                title = stringResource(R.string.let_s_talk),
                actions = {
                    NeoBrutalIconButton(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.label_prompt_history),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        onClick = { viewModel.openHistorySheet() },
                    )
                }
            )
        },
        bottomBar = {
            ChatInputArea(
                inputMode = inputMode,
                textState = textState,
                selectedImageUri = selectedImageUri,
                selectedFileName = selectedFileName,
                analysisType = analysisType,
                onModeChange = { newMode ->
                    inputMode = newMode
                    selectedImageUri = null
                    selectedFileUri = null
                },
                onTextChanged = { textState = it },
                onChipClicked = { prompt -> textState = TextFieldValue(prompt) },
                onAnalysisTypeChange = { analysisType = it },
                onClearImage = { selectedImageUri = null },
                onClearFile = { selectedFileUri = null },
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
                }
            )
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
                is ChatUiState.Success -> ContentState(state, onClearResponse = { viewModel.clearResponse() })
                is ChatUiState.Error -> ErrorState(state, onClearResponse = { viewModel.clearResponse() })
            }
        }
    }
}

@Composable
private fun ContentState(
    state: ChatUiState.Success,
    onClearResponse: () -> Unit,
) {
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
            Text(
                text = state.outputText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
            NeoBrutalIconButton(
                onClick = { onClearResponse() },
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.label_clear_response)
            )
        }
    }
}

@Composable
private fun ErrorState(
    state: ChatUiState.Error,
    onClearResponse: () -> Unit,
) {
    val (errorMsg, errorIcon) = getErrorMessageAndIcon(state.error)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.paddingLarge)
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(Dimensions.paddingMedium)
            )
            .padding(Dimensions.paddingLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = errorIcon,
                contentDescription = stringResource(R.string.label_error_icon),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimensions.paddingExtraLarge)
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.oops),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
            NeoBrutalIconButton(
                onClick = { onClearResponse() },
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.label_dismiss_error),
                backgroundColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Helper to map ChatError to UI resources
@Composable
private fun getErrorMessageAndIcon(error: ChatError): Pair<String, ImageVector> {
    val context = LocalContext.current

    return when (error) {
        is ChatError.NetworkMissing -> context.getString(R.string.error_no_internet_connection_please_check_your_network) to Icons.Rounded.WifiOff
        is ChatError.Permission -> stringResource(R.string.error_i_don_t_have_permission_to_access_that_file) to Icons.Rounded.Lock
        is ChatError.FileNotFound -> stringResource(R.string.error_i_couldn_t_find_the_selected_file) to Icons.Rounded.BrokenImage
        is ChatError.FileRead -> stringResource(R.string.error_i_couldn_t_read_the_file_content) to Icons.Rounded.Description
        is ChatError.Unknown -> (error.message ?: stringResource(R.string.error_an_unknown_error_occurred)) to Icons.Rounded.Warning
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
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
