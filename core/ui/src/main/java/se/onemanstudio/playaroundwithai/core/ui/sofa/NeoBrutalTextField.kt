package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun NeoBrutalTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = onSurfaceColor),
            cursorBrush = SolidColor(onSurfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.onSurface,
                    shadowOffset = Dimensions.neoBrutalCardShadowOffset,
                    borderWidth = Dimensions.neoBrutalCardStrokeWidth
                )
                .padding(Dimensions.paddingLarge)
        )

        if (value.text.isEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium),
                modifier = Modifier
                    .padding(Dimensions.paddingLarge)
                    .padding(start = Dimensions.paddingExtraSmall) // Slight adjustment to align with cursor
            )
        }
    }
}

@Preview(name = "Light")
@Composable
private fun NeoBrutalTextFieldLightPreview() {
    SofaAiTheme(darkTheme = false) {
        NeoBrutalTextField(
            value = TextFieldValue("User input"),
            onValueChange = {},
            placeholder = "Type something..."
        )
    }
}

@Preview(name = "Dark")
@Composable
private fun NeoBrutalTextFieldDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        NeoBrutalTextField(
            value = TextFieldValue("User input"),
            onValueChange = {},
            placeholder = "Type something..."
        )
    }
}
