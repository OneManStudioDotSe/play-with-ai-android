package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun NeoBrutalChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = Dimensions.borderStrokeSmall,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(Dimensions.chipCornerRadius)
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(Dimensions.chipCornerRadius)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Dimensions.paddingMedium, vertical = Dimensions.paddingSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(name = "Light")
@Composable
private fun NeoBrutalChipLightPreview() {
    SofaAiTheme(darkTheme = false) {
        NeoBrutalChip(text = "Suggest a recipe", onClick = {})
    }
}

@Preview(name = "Dark")
@Composable
private fun NeoBrutalChipDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        NeoBrutalChip(text = "Summarize text", onClick = {})
    }
}
