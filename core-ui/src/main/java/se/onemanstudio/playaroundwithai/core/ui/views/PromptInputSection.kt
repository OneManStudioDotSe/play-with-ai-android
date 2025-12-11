package se.onemanstudio.playaroundwithai.core.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.data.InputMode
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.neoBrutalism
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun PromptInputSection(
    textState: TextFieldValue,
    inputMode: InputMode,
    onTextChanged: (TextFieldValue) -> Unit,
    onSendClicked: () -> Unit,
    onChipClicked: (String) -> Unit,
    onAttachClicked: () -> Unit,
    onModeChange: (InputMode) -> Unit
) {
    val samplePrompts = listOf("Explain Quantum Computing", "Recipe for a cake", "Write a poem about rain")
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = Dimensions.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        if (inputMode == InputMode.TEXT) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Dimensions.paddingLarge),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
            ) {
                items(samplePrompts) { prompt ->
                    SuggestionChip(
                        border = BorderStroke(Dimensions.borderStrokeSmall, outlineColor),
                        onClick = { onChipClicked(prompt) },
                        label = { Text(prompt) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
        ) {
            ModeButton(
                text = "Text",
                isSelected = inputMode == InputMode.TEXT,
                onClick = { onModeChange(InputMode.TEXT) },
                modifier = Modifier.weight(1f)
            )
            ModeButton(
                text = "Image",
                isSelected = inputMode == InputMode.IMAGE,
                onClick = { onModeChange(InputMode.IMAGE) },
                modifier = Modifier.weight(1f)
            )
            ModeButton(
                text = "File",
                isSelected = inputMode == InputMode.DOCUMENT,
                onClick = { onModeChange(InputMode.DOCUMENT) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge),
            verticalAlignment = Alignment.Top
        ) {
            // Custom TextField
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimensions.paddingMedium)
            ) {
                BasicTextField(
                    value = textState,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism( // Apply modifier here!
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            borderColor = MaterialTheme.colorScheme.onSurface,
                            shadowOffset = Dimensions.neoBrutalCardShadowOffset,
                            borderWidth = Dimensions.neoBrutalCardStrokeWidth
                        )
                        .padding(Dimensions.paddingLarge),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            if (inputMode != InputMode.TEXT) {
                Spacer(modifier = Modifier.width(Dimensions.paddingMedium))

                if (inputMode == InputMode.IMAGE) {
                    NeoBrutalIconButton(
                        modifier = Modifier
                            .padding(end = Dimensions.paddingMedium)
                            .border(Dimensions.borderStrokeSmall, outlineColor, MaterialTheme.shapes.small),
                        onClick = onAttachClicked,
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Attach photo",
                        backgroundColor = MaterialTheme.colorScheme.secondary
                    )
                }

                if (inputMode == InputMode.DOCUMENT) {
                    NeoBrutalIconButton(
                        modifier = Modifier
                            .padding(end = Dimensions.paddingMedium)
                            .border(Dimensions.borderStrokeSmall, outlineColor, MaterialTheme.shapes.small),
                        onClick = onAttachClicked,
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Attach file",
                        backgroundColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            NeoBrutalIconButton(
                onClick = onSendClicked,
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send prompt",
                backgroundColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        border = if (!isSelected) BorderStroke(Dimensions.borderStrokeSmall, MaterialTheme.colorScheme.outline) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text)
    }
}

@Preview(showBackground = true, name = "Text Mode")
@Composable
internal fun PromptInputSection_Preview_TextMode() {
    SofaAiTheme {
        PromptInputSection(
            textState = TextFieldValue("A text-only prompt..."),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onAttachClicked = {},
            inputMode = InputMode.TEXT, // Set to Text mode
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Image Mode")
@Composable
internal fun PromptInputSection_Preview_ImageMode() {
    SofaAiTheme {
        PromptInputSection(
            textState = TextFieldValue("What's in this image?"),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onAttachClicked = {},
            inputMode = InputMode.IMAGE,
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Document Mode")
@Composable
internal fun PromptInputSection_Preview_DocumentMode() {
    SofaAiTheme {
        PromptInputSection(
            textState = TextFieldValue("Summarize this document"),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onAttachClicked = {},
            inputMode = InputMode.DOCUMENT,
            onModeChange = {}
        )
    }
}
