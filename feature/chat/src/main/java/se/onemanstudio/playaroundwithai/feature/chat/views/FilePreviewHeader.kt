package se.onemanstudio.playaroundwithai.feature.chat.views

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun FilePreviewHeader(
    fileName: String?,
    onClearFile: () -> Unit
) {
    if (fileName == null) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "File Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onClearFile) {
                Icon(Icons.Default.Close, contentDescription = "Clear file")
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode - Normal Name")
@Composable
private fun FilePreviewHeaderPreview_Normal() {
    SofaAiTheme {
        FilePreviewHeader(
            fileName = "my_document.pdf",
            onClearFile = {}
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Long Name")
@Composable
private fun FilePreviewHeaderPreview_LongName() {
    SofaAiTheme {
        FilePreviewHeader(
            fileName = "this_is_a_very_long_document_name_that_should_be_truncated.docx",
            onClearFile = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilePreviewHeaderPreview_Dark() {
    SofaAiTheme(darkTheme = true) {
        FilePreviewHeader(
            fileName = "report_q3_final.txt",
            onClearFile = {}
        )
    }
}
