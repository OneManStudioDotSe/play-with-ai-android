package se.onemanstudio.playaroundwithai.ui.screens.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.ui.theme.AIAITheme

@Composable
fun PromptInputSection(
    textState: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onSendClicked: () -> Unit,
    onChipClicked: (String) -> Unit,
    onClearClicked: () -> Unit,
    onAttachClicked: () -> Unit
) {
    val samplePrompts = listOf("Explain Quantum Computing", "Recipe for a cake", "Write a poem about rain")
    val accentColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline // This will be Yellow (Dark) or Black (Light)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        // The redundant image preview has been REMOVED from here.

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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onAttachClicked,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .border(1.dp, outlineColor, MaterialTheme.shapes.small)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Attach image",
                    tint = outlineColor
                )
            }

            OutlinedTextField(
                value = textState,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = accentColor,
                    unfocusedTextColor = accentColor,

                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,

                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = outlineColor,

                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),

                ),
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
                                contentDescription = "Clear text",
                                tint = outlineColor
                            )
                        }
                    }
                }
            )

            IconButton(
                onClick = onSendClicked,
                enabled = textState.text.isNotBlank(),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
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


@Preview(showBackground = true)
@Composable
private fun PromptInputSection_Preview_MultiLine() {
    AIAITheme {
        PromptInputSection(
            textState = TextFieldValue("This is a much longer prompt that will wrap onto multiple lines."),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onClearClicked = {},
            onAttachClicked = {}
        )
    }
}