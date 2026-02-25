package se.onemanstudio.playaroundwithai.feature.chat.views.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.data.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.data.chat.domain.model.SyncStatus
import se.onemanstudio.playaroundwithai.feature.chat.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryItemCard(
    modifier: Modifier = Modifier,
    prompt: Prompt,
    onClick: (String) -> Unit
) {
    val dateFormat = stringResource(R.string.date_format_history)
    val timeFormat = stringResource(R.string.time_format_history)
    val dateAtTimePattern = stringResource(R.string.history_date_at)

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
                val zoneId = ZoneId.systemDefault()
                val formattedDate = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault()).withZone(zoneId).format(prompt.timestamp)
                val formattedTime = DateTimeFormatter.ofPattern(timeFormat, Locale.getDefault()).withZone(zoneId).format(prompt.timestamp)

                Text(
                    text = String.format(dateAtTimePattern, formattedDate, formattedTime),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.weight(1f))

                SyncStatusIcon(syncStatus = prompt.syncStatus)
            }
        }
    }
}


@Composable
private fun SyncStatusIcon(syncStatus: SyncStatus) {
    val (icon, tint, description) = when (syncStatus) {
        SyncStatus.Pending -> Triple(
            Icons.Outlined.CloudUpload,
            MaterialTheme.colorScheme.primary,
            stringResource(R.string.sync_status_pending)
        )

        SyncStatus.Synced -> Triple(
            Icons.Outlined.CloudDone,
            MaterialTheme.colorScheme.primary,
            stringResource(R.string.sync_status_synced)
        )

        SyncStatus.Failed -> Triple(
            Icons.Outlined.CloudOff,
            MaterialTheme.colorScheme.error,
            stringResource(R.string.sync_status_failed)
        )
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = tint,
        modifier = Modifier.size(Dimensions.iconSizeSmall)
    )
}

@Preview(name = "Light - Synced")
@Composable
private fun HistoryItemCardSyncedPreview() {
    SofaAiTheme {
        HistoryItemCard(
            prompt = Prompt(
                id = 1,
                text = "What is neo-brutalism?",
                timestamp = Instant.now().minusSeconds(300),
                syncStatus = SyncStatus.Synced
            ),
            onClick = {},
            modifier = Modifier.padding(Dimensions.paddingLarge)
        )
    }
}

@Preview(name = "Dark - Pending")
@Composable
private fun HistoryItemCardPendingPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            HistoryItemCard(
                prompt = Prompt(
                    id = 2,
                    text = "Can you please give me a very detailed and long explanation of how Jetpack Compose works?",
                    timestamp = Instant.now().minusSeconds(259_200),
                    syncStatus = SyncStatus.Pending
                ),
                onClick = {},
                modifier = Modifier.padding(Dimensions.paddingLarge)
            )
        }
    }
}

@Preview(name = "Light - Failed")
@Composable
private fun HistoryItemCardFailedPreview() {
    SofaAiTheme {
        Surface {
            HistoryItemCard(
                prompt = Prompt(
                    id = 3,
                    text = "This is a prompt that should be long enough to wrap or truncate when the width is constrained.",
                    timestamp = Instant.now().minusSeconds(3_600),
                    syncStatus = SyncStatus.Failed
                ),
                onClick = {},
                modifier = Modifier
                    .padding(Dimensions.paddingLarge)
                    .width(250.dp)
            )
        }
    }
}
