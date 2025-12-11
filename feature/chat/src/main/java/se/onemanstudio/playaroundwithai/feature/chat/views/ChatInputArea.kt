package se.onemanstudio.playaroundwithai.feature.chat.views

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import se.onemanstudio.playaroundwithai.core.data.AnalysisType
import se.onemanstudio.playaroundwithai.core.data.InputMode
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.views.AnalysisHeader
import se.onemanstudio.playaroundwithai.core.ui.views.FilePreviewHeader
import se.onemanstudio.playaroundwithai.core.ui.views.PromptInputSection

@Composable
fun ChatInputArea(
    inputMode: InputMode,
    textState: TextFieldValue,
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
    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
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
                onModeChange = onModeChange,
                onTextChanged = onTextChanged,
                onChipClicked = onChipClicked,
                onAttachClicked = onAttachClicked,
                onSendClicked = onSendClicked,
            )
        }
    }
}
