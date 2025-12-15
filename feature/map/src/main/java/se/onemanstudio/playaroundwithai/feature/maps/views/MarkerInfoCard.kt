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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.maps.models.ItemOnMap
import se.onemanstudio.playaroundwithai.feature.maps.models.VehicleType

@Composable
fun MarkerInfoCard(
    marker: ItemOnMap,
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
                        imageVector = if (marker.type == VehicleType.BICYCLE) Icons.AutoMirrored.Filled.DirectionsBike else Icons.Default.ElectricScooter,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSizeLarge)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
                    Column {
                        Text(
                            text = marker.nickname.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (marker.type == VehicleType.SCOOTER) "E-Scooter" else "E-Bike",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                NeoBrutalIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    size = Dimensions.iconSizeLarge,
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
                    label = "Battery",
                    value = "${marker.batteryLevel}%"
                )
                InfoStat(
                    icon = Icons.Default.QrCode,
                    label = "Code",
                    value = marker.vehicleCode
                )
            }
        }
    }
}

@Composable
private fun InfoStat(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
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
            marker = ItemOnMap(
                id = "1",
                name = "Scooty",
                lat = 0.0,
                lng = 0.0,
                type = VehicleType.SCOOTER,
                batteryLevel = 87,
                vehicleCode = "1234",
                nickname = "Scooty"
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
            marker = ItemOnMap(
                id = "2",
                name = "Bikey",
                lat = 0.0,
                lng = 0.0,
                type = VehicleType.BICYCLE,
                batteryLevel = 55,
                vehicleCode = "6789",
                nickname = "Bikey"
            ),
            onClose = {}
        )
    }
}
