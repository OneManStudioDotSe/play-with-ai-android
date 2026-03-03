@file:Suppress("MagicNumber")

package se.onemanstudio.playaroundwithai.feature.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.neoBrutalism
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.disabledBackground
import se.onemanstudio.playaroundwithai.core.ui.theme.disabledBorder
import se.onemanstudio.playaroundwithai.core.ui.theme.disabledContent
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlueContainer
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.core.ui.theme.errorRed
import se.onemanstudio.playaroundwithai.core.ui.theme.errorRedContainer
import se.onemanstudio.playaroundwithai.core.ui.theme.solarYellow
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPinkContainer
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLimeContainer

private val SwatchWidth = 72.dp
private val SwatchHeight = 48.dp

@Immutable
private data class ColorInfo(
    val name: String,
    val color: Color,
    val hex: String,
)

private val accentColors = listOf(
    ColorInfo("electricBlue", electricBlue, "#0052FF"),
    ColorInfo("vividPink", vividPink, "#FF00A8"),
    ColorInfo("zestyLime", zestyLime, "#9EFF00"),
    ColorInfo("solarYellow", solarYellow, "#FFEB3B"),
    ColorInfo("energeticOrange", energeticOrange, "#FF9800"),
)

private val containerColors = listOf(
    ColorInfo("electricBlueContainer", electricBlueContainer, "#1A3A7A"),
    ColorInfo("vividPinkContainer", vividPinkContainer, "#7A0050"),
    ColorInfo("zestyLimeContainer", zestyLimeContainer, "#4A7A00"),
)

private val functionalColors = listOf(
    ColorInfo("errorRed", errorRed, "#D32F2F"),
    ColorInfo("errorRedContainer", errorRedContainer, "#7A1A1A"),
)

private val disabledColors = listOf(
    ColorInfo("disabledBackground", disabledBackground, "#BDBDBD"),
    ColorInfo("disabledContent", disabledContent, "#757575"),
    ColorInfo("disabledBorder", disabledBorder, "#9E9E9E"),
)

@Composable
fun ColorSection() {
    MarkerText(text = stringResource(R.string.section_colors), lineColor = vividPink)

    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

    ColorSubsection(title = stringResource(R.string.colors_accents), colors = accentColors)
    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

    ColorSubsection(title = stringResource(R.string.colors_containers), colors = containerColors)
    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

    ColorSubsection(title = stringResource(R.string.colors_functional), colors = functionalColors)
    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

    ColorSubsection(title = stringResource(R.string.colors_disabled), colors = disabledColors)
}

@Composable
private fun ColorSubsection(title: String, colors: List<ColorInfo>) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium)
    )
    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        colors.forEach { colorInfo ->
            ColorSwatch(colorInfo)
        }
    }
}

@Composable
private fun ColorSwatch(colorInfo: ColorInfo) {
    Column(modifier = Modifier.width(SwatchWidth)) {
        Box(
            modifier = Modifier
                .size(SwatchWidth, SwatchHeight)
                .neoBrutalism(
                    backgroundColor = colorInfo.color,
                    borderColor = MaterialTheme.colorScheme.onSurface
                )
        )
        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
        Text(
            text = colorInfo.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = colorInfo.hex,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium)
        )
    }
}
