package se.onemanstudio.playaroundwithai.feature.chat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.chat.R

@Composable
fun FilePreviewHeader(
    fileName: String?,
    onClearFile: () -> Unit
) {
    if (fileName == null) return

    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = stringResource(R.string.label_file_icon),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onClearFile) {
            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.label_clear_file))
        }
    }
}

@Preview(name = "Light")
@Composable
private fun FilePreviewHeaderLightPreview() {
    SofaAiTheme {
        FilePreviewHeader(
            fileName = "my_document.pdf",
            onClearFile = {}
        )
    }
}

@Preview(name = "Light - Long Name")
@Composable
private fun FilePreviewHeaderLongNamePreview() {
    SofaAiTheme {
        Surface {
            FilePreviewHeader(
                fileName = "this_is_a_very_long_document_name_that_should_be_truncated.docx",
                onClearFile = {}
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
private fun FilePreviewHeaderDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            FilePreviewHeader(
                fileName = "report_q3_final.txt",
                onClearFile = {}
            )
        }
    }
}
