package se.onemanstudio.playaroundwithai.ui.screens.views

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import se.onemanstudio.playaroundwithai.data.AnalysisType
import se.onemanstudio.playaroundwithai.ui.theme.AIAITheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisHeader(
    selectedImageUri: Uri?,
    analysisType: AnalysisType,
    onAnalysisTypeChange: (AnalysisType) -> Unit,
    onClearImage: () -> Unit
) {
    // The entire header is only displayed if an image is selected.
    if (selectedImageUri == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        var isDropdownExpanded by remember { mutableStateOf(false) }

        // Dropdown Menu for selecting the analysis type
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = analysisType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Analysis Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                AnalysisType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            onAnalysisTypeChange(type)
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preview of the selected image
        Box(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(96.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onClearImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove image",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalysisHeaderPreview() {
    AIAITheme {
        AnalysisHeader(
            selectedImageUri = Uri.EMPTY, // Use Uri.EMPTY for preview purposes
            analysisType = AnalysisType.PRODUCT,
            onAnalysisTypeChange = {},
            onClearImage = {}
        )
    }
}