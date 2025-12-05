package se.onemanstudio.playaroundwithai.ui.screens.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.theme.AIAITheme
import se.onemanstudio.playaroundwithai.data.InputMode

@Composable
fun PromptInputSection(
    textState: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onSendClicked: () -> Unit,
    onChipClicked: (String) -> Unit,
    onClearClicked: () -> Unit,
    onAttachClicked: () -> Unit,
    inputMode: InputMode,
    onModeChange: (InputMode) -> Unit
) {
    val samplePrompts = listOf("Explain Quantum Computing", "Recipe for a cake", "Write a poem about rain")
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (inputMode == InputMode.TEXT) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(samplePrompts) { prompt ->
                    SuggestionChip(
                        border = BorderStroke(1.dp, outlineColor),
                        onClick = { onChipClicked(prompt) },
                        label = { Text(prompt) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = textState,
                onValueChange = onTextChanged,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 160.dp),
                label = { Text("Enter your prompt") },
                trailingIcon = {
                    if (textState.text.isNotEmpty()) {
                        IconButton(onClick = onClearClicked) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear text"
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (inputMode != InputMode.TEXT) {
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onAttachClicked,
                    modifier = Modifier.padding(top = 10.dp).border(1.dp, outlineColor, MaterialTheme.shapes.small)
                ) {
                    if (inputMode == InputMode.IMAGE) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Attach photo",
                            tint = outlineColor
                        )
                    }

                    if (inputMode == InputMode.DOCUMENT) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Attach file",
                            tint = outlineColor
                        )
                    }
                }
            }

            IconButton(
                onClick = onSendClicked,
                enabled = textState.text.isNotBlank(),
                modifier = Modifier
                    .padding(start = 8.dp, top = 10.dp)
                    .border(1.dp, outlineColor, MaterialTheme.shapes.small)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = outlineColor
                )
            }
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
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
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
private fun PromptInputSection_Preview_TextMode() {
    AIAITheme {
        PromptInputSection(
            textState = TextFieldValue("A text-only prompt..."),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onClearClicked = {},
            onAttachClicked = {},
            inputMode = InputMode.TEXT, // Set to Text mode
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Image Mode")
@Composable
private fun PromptInputSection_Preview_ImageMode() {
    AIAITheme {
        PromptInputSection(
            textState = TextFieldValue("What's in this image?"),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onClearClicked = {},
            onAttachClicked = {},
            inputMode = InputMode.IMAGE,
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Document Mode")
@Composable
private fun PromptInputSection_Preview_DocumentMode() {
    AIAITheme {
        PromptInputSection(
            textState = TextFieldValue("Summarize this document"),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onClearClicked = {},
            onAttachClicked = {},
            inputMode = InputMode.DOCUMENT,
            onModeChange = {}
        )
    }
}
