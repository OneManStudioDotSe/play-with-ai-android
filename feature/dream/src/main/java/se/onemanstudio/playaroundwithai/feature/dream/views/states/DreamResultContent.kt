package se.onemanstudio.playaroundwithai.feature.dream.views.states

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamElement
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamLayer
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamParticle
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ElementShape
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape
import se.onemanstudio.playaroundwithai.feature.dream.R
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamImageState
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamUiState
import se.onemanstudio.playaroundwithai.feature.dream.views.FlippableDreamCard

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DreamResultContent(
    state: DreamUiState.Result?,
    imageState: DreamImageState,
    onNewDream: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val hasResult = state != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(Dimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = !hasResult,
                exit = fadeOut(),
            ) {
                NeoBrutalCard(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimensions.iconSizeXXLarge),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = hasResult,
                enter = fadeIn(),
            ) {
                if (state != null) {
                    FlippableDreamCard(
                        scene = state.scene,
                        imageState = imageState,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = hasResult,
            enter = slideInHorizontally { it } + fadeIn(),
        ) {
            if (state != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

                    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MarkerText(
                                    text = stringResource(R.string.dream_interpretation_label),
                                    lineColor = electricBlue,
                                )
                                NeoBrutalChip(
                                    text = moodDisplayName(state.mood),
                                    onClick = {},
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                            Text(
                                text = state.interpretation,
                                style = MaterialTheme.typography.bodyMedium,
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
    DreamMood.NOSTALGIC -> stringResource(R.string.dream_mood_nostalgic)
    DreamMood.HOPEFUL -> stringResource(R.string.dream_mood_hopeful)
    DreamMood.MELANCHOLIC -> stringResource(R.string.dream_mood_melancholic)
    DreamMood.ADVENTUROUS -> stringResource(R.string.dream_mood_adventurous)
    DreamMood.ROMANTIC -> stringResource(R.string.dream_mood_romantic)
}

@Suppress("MagicNumber")
private fun previewScene() = DreamScene(
    palette = DreamPalette(sky = 0xFF0D1B2A, horizon = 0xFF1B263B, accent = 0xFF415A77),
    layers = listOf(
        DreamLayer(
            depth = 0.8f,
            elements = listOf(
                DreamElement(shape = ElementShape.STAR, x = 0.5f, y = 0.3f, scale = 0.8f, color = 0xFFE0E1DD, alpha = 0.9f),
                DreamElement(shape = ElementShape.CRESCENT, x = 0.65f, y = 0.4f, scale = 1.6f, color = 0xFFE0E1DD, alpha = 0.6f),
            ),
        ),
        DreamLayer(
            depth = 0.5f,
            elements = listOf(
                DreamElement(shape = ElementShape.CRYSTAL, x = 0.75f, y = 0.5f, scale = 1.5f, color = 0xFF415A77, alpha = 0.5f),
            ),
        ),
        DreamLayer(
            depth = 0.2f,
            elements = listOf(
                DreamElement(shape = ElementShape.MOUNTAIN, x = 0.3f, y = 0.5f, scale = 2.5f, color = 0xFF1B263B, alpha = 0.6f),
                DreamElement(shape = ElementShape.TREE, x = 0.2f, y = 0.5f, scale = 1.5f, color = 0xFF415A77, alpha = 0.7f),
            ),
        ),
    ),
    particles = listOf(
        DreamParticle(shape = ParticleShape.STARBURST, count = 10, color = 0xCCE0E1DD, speed = 0.8f, size = 3f),
        DreamParticle(shape = ParticleShape.DOT, count = 8, color = 0x80415A77, speed = 0.4f, size = 2f),
    ),
)

@Preview(name = "Interpreting Light")
@Composable
private fun DreamResultContentInterpretingLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            DreamResultContent(
                state = null,
                imageState = DreamImageState.Idle,
                onNewDream = {},
            )
        }
    }
}

@Preview(name = "Interpreting Dark")
@Composable
private fun DreamResultContentInterpretingDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            DreamResultContent(
                state = null,
                imageState = DreamImageState.Idle,
                onNewDream = {},
            )
        }
    }
}

@Preview(name = "Result Light")
@Composable
private fun DreamResultContentLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            DreamResultContent(
                state = DreamUiState.Result(
                    interpretation = "Your dream of flying over a purple ocean symbolizes a desire for freedom and transcendence. " +
                            "The two moons suggest duality in your emotional life.",
                    scene = previewScene(),
                    mood = DreamMood.SURREAL,
                ),
                imageState = DreamImageState.Generated(mimeType = "image/png", artistName = "Lorem ipsum"),
                onNewDream = {},
            )
        }
    }
}

@Preview(name = "Result Dark")
@Composable
private fun DreamResultContentDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            DreamResultContent(
                state = DreamUiState.Result(
                    interpretation = "Walking through a forest of glass trees represents fragility and beauty in your subconscious.",
                    scene = previewScene(),
                    mood = DreamMood.MYSTERIOUS,
                ),
                imageState = DreamImageState.Generated(mimeType = "image/png", artistName = "Lorem ipsum"),
                onNewDream = {},
            )
        }
    }
}
