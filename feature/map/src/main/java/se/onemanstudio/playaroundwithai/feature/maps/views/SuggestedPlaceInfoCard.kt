package se.onemanstudio.playaroundwithai.feature.maps.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.map.R

@Composable
fun SuggestedPlaceInfoCard(
    place: SuggestedPlace,
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
                        imageVector = Icons.Filled.Stars,
                        contentDescription = stringResource(id = R.string.ai_suggested_place_marker_content_description, place.name),
                        modifier = Modifier.size(Dimensions.iconSizeLarge)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
                    Column {
                        Text(
                            text = place.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = place.category,
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

            Text(
                text = place.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF8F8F8)
@Composable
private fun SuggestedPlaceInfoCardPreview_Light() {
    SofaAiTheme {
        SuggestedPlaceInfoCard(
            place = SuggestedPlace(
                name = "Vasamuseet",
                lat = 59.3280,
                lng = 18.0900,
                description = "Maritime museum in Stockholm, Sweden. Located on the island of Djurgården.",
                category = "Museum"
            ),
            onClose = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF000000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SuggestedPlaceInfoCardPreview_Dark() {
    SofaAiTheme(darkTheme = true) {
        SuggestedPlaceInfoCard(
            place = SuggestedPlace(
                name = "Gröna Lund",
                lat = 59.3235,
                lng = 18.0965,
                description = "Amusement park in Stockholm, Sweden. Popular for roller coasters and concerts.",
                category = "Amusement Park"
            ),
            onClose = {}
        )
    }
}
