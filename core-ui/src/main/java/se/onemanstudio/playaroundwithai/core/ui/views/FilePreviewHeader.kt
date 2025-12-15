package se.onemanstudio.playaroundwithai.core.ui.views

import android.content.res.Configuration
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

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

@Preview(showBackground = true, name = "Light Mode - Normal Name")
@Composable
internal fun FilePreviewHeaderPreview_Normal() {
    SofaAiTheme {
        FilePreviewHeader(
            fileName = "my_document.pdf",
            onClearFile = {}
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Long Name")
@Composable
internal fun FilePreviewHeaderPreview_LongName() {
    SofaAiTheme {
        FilePreviewHeader(
            fileName = "this_is_a_very_long_document_name_that_should_be_truncated.docx",
            onClearFile = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun FilePreviewHeaderPreview_Dark() {
    SofaAiTheme(darkTheme = true) {
        FilePreviewHeader(
            fileName = "report_q3_final.txt",
            onClearFile = {}
        )
    }
}
