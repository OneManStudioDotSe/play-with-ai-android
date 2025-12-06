package se.onemanstudio.playaroundwithai.core.ui.views

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import se.onemanstudio.playaroundwithai.core.data.AnalysisType
import se.onemanstudio.playaroundwithai.core.ui.R
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Suppress("MagicNumber")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisHeader(
    selectedImageUri: Uri?,
    analysisType: AnalysisType,
    onAnalysisTypeChange: (AnalysisType) -> Unit,
    onClearImage: () -> Unit
) {
    if (selectedImageUri == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingMedium)
    ) {
        var isDropdownExpanded by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

        Box(modifier = Modifier.wrapContentSize()) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(Dimensions.imagePreviewSize)
                    .padding(all = Dimensions.paddingExtraSmall)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                error = painterResource(id = R.drawable.ic_image_placeholder)
            )
            IconButton(
                onClick = onClearImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimensions.paddingSmall)
                    .size(Dimensions.iconSizeLarge)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove image",
                    modifier = Modifier.size(Dimensions.iconSizeSmall)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun AnalysisHeaderPreview_Default() {
    SofaAiTheme {
        AnalysisHeader(
            selectedImageUri = Uri.EMPTY,
            analysisType = AnalysisType.PRODUCT,
            onAnalysisTypeChange = {},
            onClearImage = {}
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Product")
@Composable
internal fun AnalysisHeaderPreview_Light_Product() {
    SofaAiTheme {
        AnalysisHeader(
            selectedImageUri = Uri.EMPTY,
            analysisType = AnalysisType.PRODUCT,
            onAnalysisTypeChange = {},
            onClearImage = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode - Location", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AnalysisHeaderPreview_Dark_Location() {
    SofaAiTheme(darkTheme = true) {
        AnalysisHeader(
            selectedImageUri = Uri.EMPTY,
            analysisType = AnalysisType.LOCATION,
            onAnalysisTypeChange = {},
            onClearImage = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Dropdown Expanded")
@Composable
internal fun AnalysisHeaderPreview_DropdownExpanded() {
    SofaAiTheme {
        Box(modifier = Modifier.padding(bottom = Dimensions.paddingExtraLarge)) {
            Column {
                ExposedDropdownMenuBox(
                    expanded = true,
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = AnalysisType.MOVIE.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Analysis Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .padding(horizontal = Dimensions.paddingLarge)
                    )
                    ExposedDropdownMenu(
                        expanded = true,
                        onDismissRequest = {}
                    ) {
                        AnalysisType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
