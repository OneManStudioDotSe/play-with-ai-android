package se.onemanstudio.playaroundwithai.feature.chat.views.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryItemCard(
    modifier: Modifier = Modifier,
    prompt: Prompt,
    onClick: (String) -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

    NeoBrutalCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(prompt.text) }
    ) {
        Column(
            modifier = Modifier.padding(all = Dimensions.paddingMedium)
        ) {
            Text(
                text = "\"${prompt.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(Dimensions.paddingLarge))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateFormatter.format(prompt.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Preview(name = "Light - Short Text", showBackground = true, backgroundColor = 0xFFF8F8F8)
@Composable
private fun HistoryItemCardPreview_ShortText() {
    SofaAiTheme {
        HistoryItemCard(
            prompt = Prompt(
                id = 1,
                text = "What is neo-brutalism?",
                timestamp = Date(System.currentTimeMillis() - 1000 * 60 * 5) // 5 minutes ago
            ),
            onClick = {},
            modifier = Modifier.padding(Dimensions.paddingLarge)
        )
    }
}

@Preview(name = "Dark - Long Text", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun HistoryItemCardPreview_LongText() {
    SofaAiTheme(darkTheme = true) {
        HistoryItemCard(
            prompt = Prompt(
                id = 2,
                text = "Can you please give me a very detailed and long explanation of how Jetpack Compose works?",
                timestamp = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3) // 3 days ago
            ),
            onClick = {},
            modifier = Modifier.padding(Dimensions.paddingLarge)
        )
    }
}

@Preview(name = "Constrained Width", showBackground = true)
@Composable
private fun HistoryItemCardPreview_Constrained() {
    SofaAiTheme {
        HistoryItemCard(
            prompt = Prompt(
                id = 3,
                text = "This is a prompt that should be long enough to wrap or truncate when the width is constrained.",
                timestamp = Date(System.currentTimeMillis() - 1000 * 60 * 60)
            ),
            onClick = {},
            modifier = Modifier
                .padding(Dimensions.paddingLarge)
                .width(250.dp)
        )
    }
}
