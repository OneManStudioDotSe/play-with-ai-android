package se.onemanstudio.playaroundwithai.feature.chat.views

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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.data.chat.domain.model.AnalysisType
import se.onemanstudio.playaroundwithai.feature.chat.R

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
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingMedium)
    ) {
        var isDropdownExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = stringResource(id = analysisType.asStringRes()),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
                ),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.analysis_type)) },
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
                        text = { Text(stringResource(id = type.asStringRes())) },
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
                contentDescription = stringResource(R.string.label_selected_image),
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
                    .size(Dimensions.iconSizeXXLarge)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.label_remove_image),
                    modifier = Modifier.size(Dimensions.iconSizeLarge),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

fun AnalysisType.asStringRes(): Int {
    return when (this) {
        AnalysisType.LOCATION -> R.string.analysis_type_location
        AnalysisType.RECIPE -> R.string.analysis_type_recipe
        AnalysisType.MOVIE -> R.string.analysis_type_movie
        AnalysisType.SONG -> R.string.analysis_type_song
        AnalysisType.PERSONALITY -> R.string.analysis_type_personality
        AnalysisType.PRODUCT -> R.string.analysis_type_product
        AnalysisType.TREND -> R.string.analysis_type_trend
    }
}

@Preview(name = "Light")
@Composable
private fun AnalysisHeaderLightPreview() {
    SofaAiTheme {
        Surface {
            AnalysisHeader(
                selectedImageUri = Uri.EMPTY,
                analysisType = AnalysisType.PRODUCT,
                onAnalysisTypeChange = {},
                onClearImage = {}
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
private fun AnalysisHeaderDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            AnalysisHeader(
                selectedImageUri = Uri.EMPTY,
                analysisType = AnalysisType.LOCATION,
                onAnalysisTypeChange = {},
                onClearImage = {}
            )
        }
    }
}
