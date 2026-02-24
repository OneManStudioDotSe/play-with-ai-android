package se.onemanstudio.playaroundwithai.feature.chat.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.data.chat.domain.model.InputMode
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalSegmentedButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTextField
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.chat.R

private val DotSize = 12.dp
private val DotSpacing = 8.dp
private const val DOT_DELAY_UNIT = 150
private const val DOT_SCALE_MIN = 0.6f
private const val DOT_SCALE_MAX = 1.2f
private const val DOT_ANIMATION_DURATION = 600

@Composable
fun PromptInputSection(
    textState: TextFieldValue,
    inputMode: InputMode,
    suggestions: List<String>,
    isSuggestionsLoading: Boolean,
    onTextChanged: (TextFieldValue) -> Unit,
    onSendClicked: () -> Unit,
    onChipClicked: (String) -> Unit,
    onAttachClicked: () -> Unit,
    onModeChange: (InputMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(vertical = Dimensions.paddingLarge, horizontal = Dimensions.paddingSmall),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        // our conversation starters
        if (inputMode == InputMode.TEXT) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.heightMedium),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isSuggestionsLoading) {
                    ThreeDotsLoadingAnimation(
                        modifier = Modifier.padding(horizontal = Dimensions.paddingLarge)
                    )
                } else if (suggestions.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = Dimensions.paddingLarge),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
                    ) {
                        items(suggestions) { prompt ->
                            NeoBrutalChip(
                                text = prompt,
                                onClick = { onChipClicked(prompt) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

        // buttons to select a mode
        val modeLabels = InputMode.entries.map { mode ->
            when (mode) {
                InputMode.TEXT -> stringResource(R.string.input_mode_text)
                InputMode.IMAGE -> stringResource(R.string.input_mode_image)
                InputMode.DOCUMENT -> stringResource(R.string.input_mode_document)
            }
        }
        NeoBrutalSegmentedButton(
            labels = modeLabels,
            selectedIndex = InputMode.entries.indexOf(inputMode),
            onSelected = { index -> onModeChange(InputMode.entries[index]) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge)
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max) // force children to match heights
                .padding(horizontal = Dimensions.paddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoBrutalTextField(
                value = textState,
                onValueChange = onTextChanged,
                placeholder = stringResource(id = R.string.prompt_input_label),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimensions.paddingMedium)
            )

            // buttons for attachments (if at Image or Document mode)
            AnimatedVisibility(
                visible = inputMode != InputMode.TEXT,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                val icon = if (inputMode == InputMode.IMAGE) Icons.Default.AddAPhoto else Icons.Default.UploadFile
                val desc =
                    if (inputMode == InputMode.IMAGE) stringResource(R.string.label_attach_photo) else stringResource(R.string.label_attach_file)
                val bgColor = if (inputMode == InputMode.IMAGE) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary

                NeoBrutalIconButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = Dimensions.paddingMedium),
                    size = Dimensions.minButtonHeight, // Match default height of NeoBrutalTextField with padding
                    onClick = onAttachClicked,
                    imageVector = icon,
                    contentDescription = desc,
                    backgroundColor = bgColor
                )
            }

            NeoBrutalIconButton(
                modifier = Modifier.fillMaxHeight(),
                size = Dimensions.minButtonHeight, // Match default height of NeoBrutalTextField
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.label_send_prompt),
                backgroundColor = MaterialTheme.colorScheme.primary,
                onClick = onSendClicked,
            )
        }
    }
}

@Composable
fun ThreeDotsLoadingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val dotSize = DotSize
    val delayUnit = DOT_DELAY_UNIT

    @Composable
    fun Dot(delay: Int) {
        val scale by infiniteTransition.animateFloat(
            initialValue = DOT_SCALE_MIN,
            targetValue = DOT_SCALE_MAX,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = DOT_ANIMATION_DURATION, delayMillis = delay, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .size(dotSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(delay = 0)
        Spacer(modifier = Modifier.width(DotSpacing))
        Dot(delay = delayUnit)
        Spacer(modifier = Modifier.width(DotSpacing))
        Dot(delay = delayUnit * 2)
    }
}

// --- Previews ---

@Preview(name = "Light")
@Composable
private fun PromptInputSectionLightPreview() {
    SofaAiTheme(darkTheme = false) {
        PromptInputSection(
            textState = TextFieldValue(""),
            inputMode = InputMode.TEXT,
            suggestions = listOf("Tell me a joke", "Explain Quantum Physics", "Roast my code"),
            isSuggestionsLoading = false,
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onAttachClicked = {},
            onModeChange = {}
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun PromptInputSectionLoadingPreview() {
    SofaAiTheme(darkTheme = false) {
        PromptInputSection(
            textState = TextFieldValue(""),
            inputMode = InputMode.TEXT,
            suggestions = emptyList(),
            isSuggestionsLoading = true,
            onTextChanged = {},
            onSendClicked = {},
            onChipClicked = {},
            onAttachClicked = {},
            onModeChange = {}
        )
    }
}
