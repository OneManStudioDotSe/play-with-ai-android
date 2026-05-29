package se.onemanstudio.playaroundwithai.feature.nano.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.feature.nano.R
import se.onemanstudio.playaroundwithai.feature.nano.states.NanoUiState

@Composable
fun ThisDeviceSection(
    state: NanoUiState,
    onDownload: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        MarkerText(
            text = stringResource(R.string.nano_this_device_header),
            lineColor = zestyLime,
        )

        NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Dimensions.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
            ) {
                InfoRow(label = stringResource(R.string.nano_label_manufacturer), value = state.device.manufacturer)
                InfoRow(label = stringResource(R.string.nano_label_model), value = state.device.model)
                InfoRow(label = stringResource(R.string.nano_label_android), value = state.device.androidApiLevel.toString())
            }
        }

        StatusCard(phase = state.phase, onDownload = onDownload, onRetry = onRetry)
    }
}
