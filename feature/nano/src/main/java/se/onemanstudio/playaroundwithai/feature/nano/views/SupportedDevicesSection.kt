package se.onemanstudio.playaroundwithai.feature.nano.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.solarYellow
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.feature.nano.R

@Composable
fun SupportedDevicesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        MarkerText(
            text = stringResource(R.string.nano_supported_devices_header),
            lineColor = electricBlue,
        )

        Text(
            text = stringResource(R.string.nano_supported_devices_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        DeviceGroup(
            brand = stringResource(R.string.nano_brand_pixel),
            devices = stringResource(R.string.nano_devices_pixel),
            accent = electricBlue,
        )
        DeviceGroup(
            brand = stringResource(R.string.nano_brand_samsung),
            devices = stringResource(R.string.nano_devices_samsung),
            accent = vividPink,
        )
        DeviceGroup(
            brand = stringResource(R.string.nano_brand_other),
            devices = stringResource(R.string.nano_devices_other),
            accent = solarYellow,
        )
    }
}
