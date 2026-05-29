package se.onemanstudio.playaroundwithai.feature.nano.views

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.feature.nano.R
import se.onemanstudio.playaroundwithai.feature.nano.models.NanoSupport

@Composable
fun EvaluatedCard(
    support: NanoSupport,
    onDownload: () -> Unit,
) {
    val visuals = when (support) {
        NanoSupport.READY -> VerdictVisuals(
            accent = zestyLime,
            icon = Icons.Default.CheckCircle,
            titleRes = R.string.nano_status_ready_title,
            detailRes = R.string.nano_status_ready_detail,
        )

        NanoSupport.DOWNLOADABLE -> VerdictVisuals(
            accent = electricBlue,
            icon = Icons.Default.CloudDownload,
            titleRes = R.string.nano_status_downloadable_title,
            detailRes = R.string.nano_status_downloadable_detail,
        )

        NanoSupport.NOT_AVAILABLE -> VerdictVisuals(
            accent = vividPink,
            icon = Icons.Default.Block,
            titleRes = R.string.nano_status_unavailable_title,
            detailRes = R.string.nano_status_unavailable_detail,
        )
    }

    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            VerdictHeader(icon = visuals.icon, accent = visuals.accent, title = stringResource(visuals.titleRes))
            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
            Text(
                text = stringResource(visuals.detailRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (support == NanoSupport.DOWNLOADABLE) {
                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                NeoBrutalButton(
                    text = stringResource(R.string.nano_download_button),
                    icon = Icons.Default.CloudDownload,
                    iconContentDescription = stringResource(R.string.nano_download_button),
                    onClick = onDownload,
                )
            }
        }
    }
}

private data class VerdictVisuals(
    val accent: Color,
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val detailRes: Int,
)
