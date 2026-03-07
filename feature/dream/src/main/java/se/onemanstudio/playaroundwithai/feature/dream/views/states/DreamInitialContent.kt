package se.onemanstudio.playaroundwithai.feature.dream.views.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTextField
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.feature.dream.R
import se.onemanstudio.playaroundwithai.feature.dream.views.DreamGalleryRow
import java.time.Instant

@Composable
fun DreamInitialContent(
    textState: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onInterpretClick: () -> Unit,
    history: List<Dream>,
    onDreamClick: (Dream) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingLarge),
        horizontalAlignment = Alignment.Start,
    ) {
        NeoBrutalTextField(
            value = textState,
            onValueChange = onTextChanged,
            placeholder = stringResource(R.string.dream_input_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

        NeoBrutalButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.dream_interpret_button),
            enabled = textState.text.isNotBlank(),
            backgroundColor = MaterialTheme.colorScheme.primary,
            onClick = onInterpretClick,
        )

        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            MarkerText(
                text = stringResource(R.string.dream_gallery_title),
                lineColor = vividPink,
                modifier = Modifier.padding(bottom = Dimensions.paddingMedium),
            )

            DreamGalleryRow(
                modifier = Modifier.ignoreHorizontalParentPadding(Dimensions.paddingLarge),
                dreams = history,
                onDreamClick = onDreamClick,
            )
        } else {
            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            Text(
                text = stringResource(R.string.dream_empty_gallery),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun Modifier.ignoreHorizontalParentPadding(horizontal: Dp) = layout { measurable, constraints ->
    val overriddenWidth = constraints.maxWidth + 2 * horizontal.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = overriddenWidth))
    layout(placeable.width, placeable.height) {
        placeable.place(-horizontal.roundToPx(), 0)
    }
}

private fun previewDreams() = listOf(
    Dream(id = 1, description = "I was flying over a purple ocean under two moons", mood = DreamMood.SURREAL, timestamp = Instant.now()),
    Dream(id = 2, description = "Walking through a forest of glass trees", mood = DreamMood.MYSTERIOUS, timestamp = Instant.now()),
    Dream(id = 3, description = "Reunited with an old friend in a sunny meadow", mood = DreamMood.JOYFUL, timestamp = Instant.now()),
)

@Preview(name = "Initial Light")
@Composable
private fun DreamInitialContentLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            DreamInitialContent(
                textState = TextFieldValue(""),
                onTextChanged = {},
                onInterpretClick = {},
                history = previewDreams(),
                onDreamClick = {},
            )
        }
    }
}

@Preview(name = "Initial Dark")
@Composable
private fun DreamInitialContentDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            DreamInitialContent(
                textState = TextFieldValue("I was flying over an ocean"),
                onTextChanged = {},
                onInterpretClick = {},
                history = emptyList(),
                onDreamClick = {},
            )
        }
    }
}
