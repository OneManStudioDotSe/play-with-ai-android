package se.onemanstudio.playaroundwithai.core.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.data.InputMode
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalSegmentedButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTextField
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
    val samplePrompts = listOf(
        stringResource(id = R.string.sample_prompt_1),
        stringResource(id = R.string.sample_prompt_2),
        stringResource(id = R.string.sample_prompt_3)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(vertical = Dimensions.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        // 1. Suggestion Chips (Only in TEXT mode)
        if (inputMode == InputMode.TEXT) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Dimensions.paddingLarge),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
            ) {
                items(samplePrompts) { prompt ->
                    NeoBrutalChip(
                        text = prompt,
                        onClick = { onChipClicked(prompt) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

        // 2. Mode Selector (Segmented Button)
        NeoBrutalSegmentedButton(
            modes = InputMode.entries,
            selectedMode = inputMode,
            onModeSelected = onModeChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge)
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

        // 3. Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge),
            verticalAlignment = Alignment.Top
        ) {
            NeoBrutalTextField(
                value = textState,
                onValueChange = onTextChanged,
                placeholder = stringResource(id = R.string.prompt_input_label),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimensions.paddingMedium)
            )

            // Attachment Buttons
            if (inputMode != InputMode.TEXT) {
                val icon = if (inputMode == InputMode.IMAGE) Icons.Default.AddAPhoto else Icons.Default.UploadFile
                val desc =
                    if (inputMode == InputMode.IMAGE) stringResource(R.string.label_attach_photo) else stringResource(R.string.label_attach_file)
                val bgColor = if (inputMode == InputMode.IMAGE) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary

                NeoBrutalIconButton(
                    modifier = Modifier.padding(end = Dimensions.paddingMedium),
                    onClick = onAttachClicked,
                    imageVector = icon,
                    contentDescription = desc,
                    backgroundColor = bgColor
                )
            }

            // Send Button
            NeoBrutalIconButton(
                onClick = onSendClicked,
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.label_send_prompt),
                backgroundColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "Full Layout - Light")
@Composable
private fun PromptInputSectionLightPreview() {
    SofaAiTheme(darkTheme = false) {
        PromptInputSection(
            textState = TextFieldValue(""),
            inputMode = InputMode.TEXT,
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onAttachClicked = {},
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Full Layout - Dark")
@Composable
private fun PromptInputSectionDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        PromptInputSection(
            textState = TextFieldValue("Describe this image"),
            inputMode = InputMode.IMAGE,
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onAttachClicked = {},
            onModeChange = {}
        )
    }
}
