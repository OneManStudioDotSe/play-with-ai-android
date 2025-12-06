package se.onemanstudio.playaroundwithai.core.ui.views

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.data.domain.model.Prompt
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
    // A memoized formatter to avoid recreating it on every recomposition
    val dateFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    }

    NeoBrutalCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(prompt.text) }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingLarge)
        ) {
            // Main prompt text
            Text(
                text = "\"${prompt.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(Dimensions.paddingMedium))

            // Timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Used on:",
                    style = MaterialTheme.typography.labelMedium, // Using a smaller label style
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dateFormatter.format(Date(prompt.timestamp)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary, // Use a vibrant color
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Preview(name = "Light Theme - Short Text", showBackground = true, backgroundColor = 0xFFF8F8F8)
@Composable
private fun HistoryItemCardPreview_ShortText() {
    SofaAiTheme {
        HistoryItemCard(
            prompt = Prompt(
                id = 1,
                text = "What is neo-brutalism?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5 // 5 minutes ago
            ),
            onClick = {},
            modifier = Modifier.padding(Dimensions.paddingLarge)
        )
    }
}

@Preview(name = "Dark Theme - Long Text", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun HistoryItemCardPreview_LongText() {
    SofaAiTheme(darkTheme = true) {
        HistoryItemCard(
            prompt = Prompt(
                id = 2,
                text = "Can you please give me a very detailed and long explanation of how Jetpack Compose recomposition works under the hood, including implementation details?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3 // 3 days ago
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
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 // 1 hour ago
            ),
            onClick = {},
            modifier = Modifier
                .padding(Dimensions.paddingLarge)
                .width(250.dp) // Simulate a narrow screen
        )
    }
}
