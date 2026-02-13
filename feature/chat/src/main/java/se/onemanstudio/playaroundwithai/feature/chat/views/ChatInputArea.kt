package se.onemanstudio.playaroundwithai.feature.chat.views

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.InputMode
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun ChatInputArea(
    inputMode: InputMode,
    textState: TextFieldValue,
    suggestions: List<String>,
    isSuggestionsLoading: Boolean,
    selectedImageUri: Uri?,
    selectedFileName: String?,
    analysisType: AnalysisType,
    onModeChange: (InputMode) -> Unit,
    onTextChanged: (TextFieldValue) -> Unit,
    onChipClicked: (String) -> Unit,
    onAnalysisTypeChange: (AnalysisType) -> Unit,
    onClearImage: () -> Unit,
    onClearFile: () -> Unit,
    onAttachClicked: () -> Unit,
    onSendClicked: () -> Unit
) {
    NeoBrutalCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(alignment = Alignment.BottomCenter)
    ) {
        Column(modifier = Modifier.padding(top = Dimensions.paddingMedium)) {
            when (inputMode) {
                InputMode.IMAGE -> AnalysisHeader(
                    selectedImageUri = selectedImageUri,
                    analysisType = analysisType,
                    onAnalysisTypeChange = onAnalysisTypeChange,
                    onClearImage = onClearImage
                )

                InputMode.DOCUMENT -> FilePreviewHeader(
                    fileName = selectedFileName,
                    onClearFile = onClearFile
                )

                InputMode.TEXT -> {}
            }

            PromptInputSection(
                textState = textState,
                inputMode = inputMode,
                suggestions = suggestions,
                isSuggestionsLoading = isSuggestionsLoading,
                onModeChange = onModeChange,
                onTextChanged = onTextChanged,
                onChipClicked = onChipClicked,
                onAttachClicked = onAttachClicked,
                onSendClicked = onSendClicked,
            )
        }
    }
}

@Preview(name = "Text Mode")
@Composable
private fun ChatInputAreaTextModePreview() {
    SofaAiTheme {
        ChatInputArea(
            inputMode = InputMode.TEXT,
            textState = TextFieldValue("What is the meaning of life?"),
            suggestions = listOf("Tell me a joke", "Explain Quantum Physics", "Roast my code"),
            isSuggestionsLoading = false,
            selectedImageUri = null,
            selectedFileName = null,
            analysisType = AnalysisType.LOCATION,
            onModeChange = {},
            onTextChanged = {},
            onChipClicked = {},
            onAnalysisTypeChange = {},
            onClearImage = {},
            onClearFile = {},
            onAttachClicked = {},
            onSendClicked = {}
        )
    }
}

@Preview(name = "Image Mode")
@Composable
private fun ChatInputAreaImageModePreview() {
    SofaAiTheme {
        ChatInputArea(
            inputMode = InputMode.IMAGE,
            textState = TextFieldValue("What do you see at this image?"),
            suggestions = listOf("Tell me a joke", "Explain Quantum Physics", "Roast my code"),
            isSuggestionsLoading = false,
            selectedImageUri = null,
            selectedFileName = null,
            analysisType = AnalysisType.LOCATION,
            onModeChange = {},
            onTextChanged = {},
            onChipClicked = {},
            onAnalysisTypeChange = {},
            onClearImage = {},
            onClearFile = {},
            onAttachClicked = {},
            onSendClicked = {}
        )
    }
}

@Preview(name = "Document Mode")
@Composable
private fun ChatInputAreaDocumentModePreview() {
    SofaAiTheme {
        ChatInputArea(
            inputMode = InputMode.DOCUMENT,
            textState = TextFieldValue("Summarize this document in a funny way"),
            suggestions = listOf("Tell me a joke", "Explain Quantum Physics", "Roast my code"),
            isSuggestionsLoading = false,
            selectedImageUri = null,
            selectedFileName = null,
            analysisType = AnalysisType.LOCATION,
            onModeChange = {},
            onTextChanged = {},
            onChipClicked = {},
            onAnalysisTypeChange = {},
            onClearImage = {},
            onClearFile = {},
            onAttachClicked = {},
            onSendClicked = {}
        )
    }
}
