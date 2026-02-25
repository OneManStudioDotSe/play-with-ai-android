@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.dream

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTextField
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamElement
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamLayer
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamParticle
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ElementShape
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamError
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamUiState
import se.onemanstudio.playaroundwithai.feature.dream.views.DreamGalleryRow
import se.onemanstudio.playaroundwithai.feature.dream.views.DreamscapeCanvas
import java.time.Instant

private const val CANVAS_HEIGHT_MIN = 180
private const val CANVAS_HEIGHT_MAX = 280

@Composable
fun DreamScreen(
    viewModel: DreamViewModel = hiltViewModel(),
    settingsContent: @Composable (() -> Unit) -> Unit = { _ -> },
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val uiState = screenState.dreamState
    val history = screenState.dreamHistory

    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var showSettings by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is DreamUiState.Result -> view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            is DreamUiState.Error -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            else -> {}
        }
    }

    if (showSettings) {
        settingsContent { showSettings = false }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoBrutalTopAppBar(
                title = stringResource(R.string.dream_title),
                actions = {
                    NeoBrutalIconButton(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(
                            se.onemanstudio.playaroundwithai.core.ui.views.R.string.settings_icon_description
                        ),
                        onClick = { showSettings = true },
                    )
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                is DreamUiState.Initial -> InitialState(
                    textState = textState,
                    onTextChanged = { textState = it },
                    onInterpretClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.interpretDream(textState.text)
                        keyboardController?.hide()
                    },
                    history = history,
                    onDreamClick = { dream -> viewModel.restoreDream(dream) },
                )

                is DreamUiState.Interpreting -> InterpretingState()
                is DreamUiState.Result -> ResultState(
                    state = uiState,
                    onNewDream = {
                        textState = TextFieldValue("")
                        viewModel.clearResult()
                    },
                )

                is DreamUiState.Error -> ErrorState(
                    state = uiState,
                    onClearError = { viewModel.clearResult() },
                )
            }
        }
    }
}

@Composable
private fun InitialState(
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
            modifier = Modifier.wrapContentSize(),
            text = stringResource(R.string.dream_interpret_button),
            enabled = textState.text.isNotBlank(),
            backgroundColor = MaterialTheme.colorScheme.primary,
            onClick = onInterpretClick,
        )

        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            Text(
                text = stringResource(R.string.dream_gallery_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = Dimensions.paddingMedium),
            )

            DreamGalleryRow(
                dreams = history,
                onDreamClick = onDreamClick,
            )
        } else {
            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            Text(
                text = stringResource(R.string.dream_empty_gallery),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = se.onemanstudio.playaroundwithai.core.ui.theme.Alphas.medium
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InterpretingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimensions.iconSizeXXLarge),
        )
        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
        Text(
            text = stringResource(R.string.dream_interpreting),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@Composable
private fun ResultState(
    state: DreamUiState.Result,
    onNewDream: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        DreamscapeCanvas(
            scene = state.scene,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CANVAS_HEIGHT_MIN.dp, max = CANVAS_HEIGHT_MAX.dp),
        )

        Column(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NeoBrutalChip(
                text = moodDisplayName(state.mood),
                onClick = {},
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                    Text(
                        text = stringResource(R.string.dream_interpretation_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                    Text(
                        text = state.interpretation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            NeoBrutalButton(
                text = stringResource(R.string.dream_new_button),
                backgroundColor = MaterialTheme.colorScheme.secondary,
                onClick = onNewDream,
            )
        }
    }
}

@Composable
private fun ErrorState(
    state: DreamUiState.Error,
    onClearError: () -> Unit,
) {
    val (errorMsg, errorIcon) = getErrorMessageAndIcon(state.error)

    NeoBrutalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.paddingLarge),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Dimensions.paddingLarge),
        ) {
            Icon(
                imageVector = errorIcon,
                contentDescription = stringResource(R.string.dream_label_error_icon),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimensions.iconSizeXLarge),
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.dream_oops),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (state.error !is DreamError.ApiKeyMissing) {
                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                NeoBrutalIconButton(
                    onClick = onClearError,
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.dream_label_dismiss_error),
                    backgroundColor = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun getErrorMessageAndIcon(error: DreamError): Pair<String, ImageVector> {
    return when (error) {
        is DreamError.ApiKeyMissing -> stringResource(R.string.dream_error_api_key_missing) to Icons.Rounded.VpnKey
        is DreamError.NetworkMissing -> stringResource(R.string.dream_error_network) to Icons.Rounded.WifiOff
        is DreamError.InputTooLong -> stringResource(R.string.dream_error_input_too_long) to Icons.Rounded.Warning
        is DreamError.Unknown -> (error.message ?: stringResource(R.string.dream_error_unknown)) to Icons.Rounded.Warning
    }
}

@Composable
private fun moodDisplayName(mood: DreamMood): String = when (mood) {
    DreamMood.JOYFUL -> stringResource(R.string.dream_mood_joyful)
    DreamMood.MYSTERIOUS -> stringResource(R.string.dream_mood_mysterious)
    DreamMood.ANXIOUS -> stringResource(R.string.dream_mood_anxious)
    DreamMood.PEACEFUL -> stringResource(R.string.dream_mood_peaceful)
    DreamMood.DARK -> stringResource(R.string.dream_mood_dark)
    DreamMood.SURREAL -> stringResource(R.string.dream_mood_surreal)
}

// region Previews

@Suppress("MagicNumber")
private fun previewScene() = DreamScene(
    palette = DreamPalette(sky = 0xFF0D1B2A, horizon = 0xFF1B263B, accent = 0xFF415A77),
    layers = listOf(
        DreamLayer(
            depth = 0.2f,
            elements = listOf(
                DreamElement(shape = ElementShape.MOUNTAIN, x = 0.3f, y = 0.7f, scale = 2.5f, color = 0xFF1B263B, alpha = 0.6f),
                DreamElement(shape = ElementShape.CRYSTAL, x = 0.75f, y = 0.65f, scale = 1.5f, color = 0xFF415A77, alpha = 0.5f),
            ),
        ),
        DreamLayer(
            depth = 0.5f,
            elements = listOf(
                DreamElement(shape = ElementShape.TREE, x = 0.2f, y = 0.8f, scale = 1.5f, color = 0xFF415A77, alpha = 0.7f),
                DreamElement(shape = ElementShape.CRESCENT, x = 0.65f, y = 0.25f, scale = 1.6f, color = 0xFFE0E1DD, alpha = 0.6f),
            ),
        ),
        DreamLayer(
            depth = 0.8f,
            elements = listOf(
                DreamElement(shape = ElementShape.STAR, x = 0.5f, y = 0.15f, scale = 0.8f, color = 0xFFE0E1DD, alpha = 0.9f),
            ),
        ),
    ),
    particles = listOf(
        DreamParticle(shape = ParticleShape.STARBURST, count = 12, color = 0xCCE0E1DD, speed = 0.8f, size = 3f),
        DreamParticle(shape = ParticleShape.DOT, count = 8, color = 0x80415A77, speed = 0.4f, size = 2f),
    ),
)

@Suppress("MagicNumber")
private fun previewDreams() = listOf(
    Dream(id = 1, description = "I was flying over a purple ocean under two moons", mood = DreamMood.SURREAL, timestamp = Instant.now()),
    Dream(id = 2, description = "Walking through a forest of glass trees", mood = DreamMood.MYSTERIOUS, timestamp = Instant.now()),
    Dream(id = 3, description = "Reunited with an old friend in a sunny meadow", mood = DreamMood.JOYFUL, timestamp = Instant.now()),
)

@Preview(name = "Initial Light")
@Composable
private fun InitialStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            InitialState(
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
private fun InitialStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            InitialState(
                textState = TextFieldValue("I was flying over an ocean"),
                onTextChanged = {},
                onInterpretClick = {},
                history = emptyList(),
                onDreamClick = {},
            )
        }
    }
}

@Preview(name = "Interpreting Light")
@Composable
private fun InterpretingStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            InterpretingState()
        }
    }
}

@Preview(name = "Interpreting Dark")
@Composable
private fun InterpretingStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            InterpretingState()
        }
    }
}

@Preview(name = "Result Light")
@Composable
private fun ResultStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            ResultState(
                state = DreamUiState.Result(
                    interpretation = "Your dream of flying over a purple ocean symbolizes a desire for freedom and transcendence. " +
                            "The two moons suggest duality in your emotional life.",
                    scene = previewScene(),
                    mood = DreamMood.SURREAL,
                ),
                onNewDream = {},
            )
        }
    }
}

@Preview(name = "Result Dark")
@Composable
private fun ResultStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            ResultState(
                state = DreamUiState.Result(
                    interpretation = "Walking through a forest of glass trees represents fragility and beauty in your subconscious.",
                    scene = previewScene(),
                    mood = DreamMood.MYSTERIOUS,
                ),
                onNewDream = {},
            )
        }
    }
}

@Preview(name = "Error Light")
@Composable
private fun ErrorStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            ErrorState(
                state = DreamUiState.Error(error = DreamError.NetworkMissing),
                onClearError = {},
            )
        }
    }
}

@Preview(name = "Error Dark")
@Composable
private fun ErrorStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            ErrorState(
                state = DreamUiState.Error(error = DreamError.ApiKeyMissing),
                onClearError = {},
            )
        }
    }
}

// endregion
