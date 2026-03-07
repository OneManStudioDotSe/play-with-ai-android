package se.onemanstudio.playaroundwithai.feature.showcase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalSegmentedButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.IbmPlexMono
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TypographySection() {
    val typography = MaterialTheme.typography

    MarkerText(text = stringResource(R.string.section_typography), lineColor = electricBlue)

    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

    FontWeightCard()

    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

    var selectedIndex by remember { mutableIntStateOf(0) }

    NeoBrutalSegmentedButton(
        labels = listOf(stringResource(R.string.toggle_standard), stringResource(R.string.toggle_emphasized)),
        selectedIndex = selectedIndex,
        onSelected = { selectedIndex = it },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

    val styles = if (selectedIndex == 0) standardStyles(typography) else emphasizedStyles(typography)

    styles.forEach { (name, style) ->
        TextStyleItem(name = name, style = style)
    }
}

@Composable
private fun FontWeightCard() {
    val sampleText = stringResource(R.string.font_sample_text)

    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            FontWeightRow(stringResource(R.string.font_weight_normal), sampleText, FontWeight.Normal)
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            FontWeightRow(stringResource(R.string.font_weight_medium), sampleText, FontWeight.Medium)
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            FontWeightRow(stringResource(R.string.font_weight_semibold), sampleText, FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            FontWeightRow(stringResource(R.string.font_weight_bold), sampleText, FontWeight.Bold)
        }
    }
}

@Composable
private fun FontWeightRow(label: String, sampleText: String, fontWeight: FontWeight) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium)
    )
    Text(
        text = sampleText,
        style = MaterialTheme.typography.titleMedium.copy(fontFamily = IbmPlexMono, fontWeight = fontWeight)
    )
}

@Composable
private fun TextStyleItem(name: String, style: TextStyle) {
    Text(
        text = name,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium)
    )
    Text(
        text = name,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
}

private fun standardStyles(typography: androidx.compose.material3.Typography): List<Pair<String, TextStyle>> = listOf(
    "Display Large" to typography.displayLarge,
    "Display Medium" to typography.displayMedium,
    "Display Small" to typography.displaySmall,
    "Headline Large" to typography.headlineLarge,
    "Headline Medium" to typography.headlineMedium,
    "Headline Small" to typography.headlineSmall,
    "Title Large" to typography.titleLarge,
    "Title Medium" to typography.titleMedium,
    "Title Small" to typography.titleSmall,
    "Body Large" to typography.bodyLarge,
    "Body Medium" to typography.bodyMedium,
    "Body Small" to typography.bodySmall,
    "Label Large" to typography.labelLarge,
    "Label Medium" to typography.labelMedium,
    "Label Small" to typography.labelSmall,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun emphasizedStyles(typography: androidx.compose.material3.Typography): List<Pair<String, TextStyle>> = listOf(
    "Display Large" to typography.displayLargeEmphasized,
    "Display Medium" to typography.displayMediumEmphasized,
    "Display Small" to typography.displaySmallEmphasized,
    "Headline Large" to typography.headlineLargeEmphasized,
    "Headline Medium" to typography.headlineMediumEmphasized,
    "Headline Small" to typography.headlineSmallEmphasized,
    "Title Large" to typography.titleLargeEmphasized,
    "Title Medium" to typography.titleMediumEmphasized,
    "Title Small" to typography.titleSmallEmphasized,
    "Body Large" to typography.bodyLargeEmphasized,
    "Body Medium" to typography.bodyMediumEmphasized,
    "Body Small" to typography.bodySmallEmphasized,
    "Label Large" to typography.labelLargeEmphasized,
    "Label Medium" to typography.labelMediumEmphasized,
    "Label Small" to typography.labelSmallEmphasized,
)
