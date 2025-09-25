package se.onemanstudio.playaroundwithai.ui.screens.views

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import se.onemanstudio.playaroundwithai.R
import se.onemanstudio.playaroundwithai.ui.theme.AIAITheme
import se.onemanstudio.playaroundwithai.ui.theme.PureBlack

@Composable
fun PromptInputSection(
    textState: TextFieldValue,
    selectedImageUri: Uri?,
    onTextChanged: (TextFieldValue) -> Unit,
    onSendClicked: () -> Unit,
    onChipClicked: (String) -> Unit,
    onClearClicked: () -> Unit,
    onClearImage: () -> Unit,
    onAttachClicked: () -> Unit
) {
    val samplePrompts = listOf("Explain Quantum Computing", "Recipe for a cake", "Write a poem about rain")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 8.dp) // Adjusted padding
    ) {
        // Image preview section
        selectedImageUri?.let { uri ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .size(96.dp)
                        .padding(all = 16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.bone),
                    error = painterResource(R.drawable.bone),

                    )
                IconButton(
                    onClick = onClearImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove image", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(samplePrompts) { prompt ->
                SuggestionChip(
                    onClick = { onChipClicked(prompt) },
                    label = { Text(prompt) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically // Keeps icons centered
        ) {
            // Attach file button
            IconButton(onClick = onAttachClicked) {
                Icon(
                    tint = PureBlack,
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Attach image"
                )
            }

            OutlinedTextField(
                value = textState,
                onValueChange = onTextChanged,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 160.dp), // Key change for dynamic height
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
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSendClicked, enabled = textState.text.isNotBlank()) {
                Icon(
                    tint = PureBlack,
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PromptInputSection_Preview_Empty() {
    AIAITheme {
        PromptInputSection(
            textState = TextFieldValue(""),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onClearClicked = {},
            selectedImageUri = null,
            onClearImage = {},
            onAttachClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PromptInputSection_Preview_MultiLine() {
    AIAITheme {
        PromptInputSection(
            textState = TextFieldValue("This is a much longer prompt that will wrap onto multiple lines,\nshowing how the text field grows automatically to accommodate the content."),
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onClearClicked = {},
            selectedImageUri = null,
            onClearImage = {},
            onAttachClicked = {}
        )
    }
}
