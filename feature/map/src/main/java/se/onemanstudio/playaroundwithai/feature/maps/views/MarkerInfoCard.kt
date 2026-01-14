package se.onemanstudio.playaroundwithai.feature.maps.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.map.R
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel

@Composable
fun MarkerInfoCard(
    marker: MapItemUiModel,
    onClose: () -> Unit
) {
    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (marker.mapItem.type == VehicleType.BICYCLE) {
                            Icons.AutoMirrored.Filled.DirectionsBike
                        } else {
                            Icons.Default.ElectricScooter
                        },
                        contentDescription = stringResource(id = R.string.vehicle_type_icon_content_description),
                        modifier = Modifier.size(Dimensions.iconSizeLarge)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
                    Column {
                        Text(
                            text = marker.mapItem.nickname.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (marker.mapItem.type == VehicleType.SCOOTER) stringResource(R.string.e_scooter) else stringResource(R.string.e_bike),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                NeoBrutalIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    size = Dimensions.iconSizeXLarge,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    onClick = onClose,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoStat(
                    icon = Icons.Default.BatteryStd,
                    iconContentDescription = stringResource(id = R.string.battery_icon_content_description),
                    label = stringResource(R.string.battery),
                    value = "${marker.mapItem.batteryLevel}%"
                )
                InfoStat(
                    icon = Icons.Default.QrCode,
                    iconContentDescription = stringResource(id = R.string.code_icon_content_description),
                    label = stringResource(R.string.code),
                    value = marker.mapItem.vehicleCode
                )
            }
        }
    }
}

@Composable
private fun InfoStat(
    icon: ImageVector,
    iconContentDescription: String,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(Dimensions.paddingSmall))
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(name = "Scooter")
@Composable
fun MarkerInfoCardPreview_Scooter() {
    SofaAiTheme {
        MarkerInfoCard(
            marker = MapItemUiModel(
                mapItem = MapItem(
                    id = "1",
                    name = "Scooty",
                    lat = 0.0,
                    lng = 0.0,
                    type = VehicleType.SCOOTER,
                    batteryLevel = 87,
                    vehicleCode = "1234",
                    nickname = "Scooty"
                )
            ),
            onClose = {}
        )
    }
}

@Preview(name = "Bicycle")
@Composable
fun MarkerInfoCardPreview_Bicycle() {
    SofaAiTheme {
        MarkerInfoCard(
            marker = MapItemUiModel(
                mapItem = MapItem(
                    id = "2",
                    name = "Bikey",
                    lat = 0.0,
                    lng = 0.0,
                    type = VehicleType.BICYCLE,
                    batteryLevel = 55,
                    vehicleCode = "6789",
                    nickname = "Bikey"
                )
            ),
            onClose = {}
        )
    }
}
