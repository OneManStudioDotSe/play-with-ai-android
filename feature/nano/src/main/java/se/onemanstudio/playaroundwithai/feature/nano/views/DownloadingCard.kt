package se.onemanstudio.playaroundwithai.feature.nano.views

import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.feature.nano.R
import se.onemanstudio.playaroundwithai.feature.nano.states.NanoPhase

@Composable
fun DownloadingCard(phase: NanoPhase.Downloading) {
    val context = LocalContext.current
    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            VerdictHeader(
                icon = Icons.Default.CloudDownload,
                accent = electricBlue,
                title = stringResource(R.string.nano_downloading),
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

            if (phase.totalBytes > 0L) {
                LinearProgressIndicator(
                    progress = { phase.downloadedBytes.toFloat() / phase.totalBytes.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.extraLow),
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                Text(
                    text = stringResource(
                        R.string.nano_download_progress,
                        Formatter.formatShortFileSize(context, phase.downloadedBytes),
                        Formatter.formatShortFileSize(context, phase.totalBytes),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.extraLow),
                )
            }
        }
    }
}
