package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun NeoBrutalCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.neoBrutalism(
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MaterialTheme.colorScheme.onSurface,
            shadowOffset = Dimensions.paddingSmall
        )
    ) {
        content()
    }
}

@Preview(name = "Light")
@Composable
private fun NeoBrutalCardLightPreview() {
    SofaAiTheme {
        NeoBrutalCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge)
        ) {
            Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                Text(
                    text = "Card Title",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = "This is a card. It uses the neoBrutalism modifier to get a background, border, and shadow.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(name = "Dark")
@Composable
private fun NeoBrutalCardDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        NeoBrutalCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge)
        ) {
            Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                Text(
                    text = "Card Title",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = "This is a card. It uses the neoBrutalism modifier to get a background, border, and shadow.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
