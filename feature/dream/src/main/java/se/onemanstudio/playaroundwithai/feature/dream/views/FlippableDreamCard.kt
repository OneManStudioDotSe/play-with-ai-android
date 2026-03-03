@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.dream.views

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamElement
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamLayer
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamParticle
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ElementShape
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape
import se.onemanstudio.playaroundwithai.feature.dream.R
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamImageState
import timber.log.Timber

private const val CANVAS_HEIGHT_MAX = 280
private const val ARTIST_LABEL_ALPHA = 0.85f
private const val FLIP_DURATION_MS = 600
private const val FLIP_HALF_ANGLE = 90f
private const val FLIP_FULL_ANGLE = 180f
private const val CAMERA_DISTANCE_FACTOR = 12f
private const val PLACEHOLDER_ICON_ALPHA = 0.4f

@Composable
fun FlippableDreamCard(
    modifier: Modifier = Modifier,
    scene: DreamScene,
    imageState: DreamImageState
) {
    val isImageReady = imageState is DreamImageState.Generated
    var isFlipped by remember { mutableStateOf(false) }

    LaunchedEffect(isImageReady) {
        if (!isImageReady) isFlipped = false
    }

    LaunchedEffect(scene, imageState) {
        Timber.d(
            "FlippableCard - scene layers=%d, imageState=%s, isFlipped=%s",
            scene.layers.size, imageState::class.simpleName, isFlipped,
        )
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) FLIP_FULL_ANGLE else 0f,
        animationSpec = tween(durationMillis = FLIP_DURATION_MS),
        label = "cardFlip",
    )

    NeoBrutalCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isImageReady) {
                    Modifier.clickable { isFlipped = !isFlipped }
                } else {
                    Modifier
                }
            )
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = CAMERA_DISTANCE_FACTOR * density
            },
    ) {
        if (rotation < FLIP_HALF_ANGLE) {
            FrontSide(scene = scene, isImageReady = isImageReady)
        } else {
            BackSide(imageState = imageState, scene = scene)
        }
    }
}

@Composable
private fun FrontSide(
    scene: DreamScene,
    isImageReady: Boolean,
) {
    Timber.d("FrontSide - isImageReady: $isImageReady and scene is $scene")
    Box {
        DreamscapeCanvas(
            scene = scene,
            modifier = Modifier
                .fillMaxWidth()
                .height(CANVAS_HEIGHT_MAX.dp)
                .clipToBounds(),
        )

        if (isImageReady) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimensions.paddingMedium),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Autorenew,
                    contentDescription = stringResource(R.string.dream_flip_hint),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(Dimensions.iconSizeLarge),
                )
            }
        }
    }
}

@Composable
private fun BackSide(imageState: DreamImageState, scene: DreamScene) {
    Box(
        modifier = Modifier.graphicsLayer { rotationY = FLIP_FULL_ANGLE },
    ) {
        val generated = imageState as? DreamImageState.Generated

        val bitmap = remember(generated?.imagePath, generated?.imageBase64) {
            generated?.imagePath?.let { path ->
                BitmapFactory.decodeFile(path)
            } ?: generated?.imageBase64?.let { base64 ->
                val bytes = java.util.Base64.getDecoder().decode(base64)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.dream_image_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CANVAS_HEIGHT_MAX.dp),
            )
        } else {
            PlaceholderSurface(scene = scene)
        }

        if (generated != null) {
            ArtistOverlay(
                artistName = generated.artistName,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun PlaceholderSurface(scene: DreamScene) {
    val palette = scene.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CANVAS_HEIGHT_MAX.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(palette.sky.toInt()),
                        androidx.compose.ui.graphics.Color(palette.horizon.toInt()),
                        androidx.compose.ui.graphics.Color(palette.accent.toInt()),
                    ),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Palette,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.White.copy(alpha = PLACEHOLDER_ICON_ALPHA),
            modifier = Modifier
                .size(64.dp)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun ArtistOverlay(
    artistName: String,
    modifier: Modifier = Modifier,
) {
    var showArtist by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(Dimensions.paddingMedium)) {
        NeoBrutalIconButton(
            imageVector = Icons.Rounded.QuestionMark,
            contentDescription = stringResource(R.string.dream_artist_hint),
            size = Dimensions.iconSizeSmall,
            backgroundColor = MaterialTheme.colorScheme.surface,
            onClick = { showArtist = !showArtist },
            modifier = Modifier.align(Alignment.BottomEnd),
        )

        AnimatedVisibility(
            visible = showArtist,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Dimensions.paddingExtraLarge),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = ARTIST_LABEL_ALPHA),
                shape = CircleShape,
            ) {
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = Dimensions.paddingLarge,
                        vertical = Dimensions.paddingMedium,
                    ),
                )
            }
        }
    }
}

// region Previews

@Suppress("MagicNumber")
private fun previewDarkScene() = DreamScene(
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
            depth = 0.2f,
            elements = listOf(
                DreamElement(shape = ElementShape.MOUNTAIN, x = 0.3f, y = 0.5f, scale = 2.5f, color = 0xFF1B263B, alpha = 0.6f),
                DreamElement(shape = ElementShape.TREE, x = 0.2f, y = 0.5f, scale = 1.5f, color = 0xFF415A77, alpha = 0.7f),
            ),
        ),
    ),
    particles = listOf(
        DreamParticle(shape = ParticleShape.STARBURST, count = 10, color = 0xCCE0E1DD, speed = 0.8f, size = 3f),
    ),
)

@Suppress("MagicNumber", "LongMethod")
private fun previewWarmScene() = DreamScene(
    palette = DreamPalette(
        sky = 0xFFFF6B35,
        horizon = 0xFFFF9F1C,
        accent = 0xFFFFBF69
    ),
    layers = listOf(
        DreamLayer(
            depth = 0.8f,
            elements = listOf(
                DreamElement(
                    shape = ElementShape.CIRCLE,
                    x = 0.7f,
                    y = 0.3f,
                    scale = 2.0f,
                    color = 0xFFFFD700,
                    alpha = 0.8f
                ),
                DreamElement(
                    shape = ElementShape.CLOUD,
                    x = 0.3f,
                    y = 0.4f,
                    scale = 1.5f,
                    color = 0xCCFFFFFF,
                    alpha = 0.6f
                ),
            ),
        ),
        DreamLayer(
            depth = 0.2f,
            elements = listOf(
                DreamElement(
                    shape = ElementShape.WAVE,
                    x = 0.5f,
                    y = 0.5f,
                    scale = 3.0f,
                    color = 0xFF4682B4,
                    alpha = 0.5f
                ),
                DreamElement(
                    shape = ElementShape.LOTUS,
                    x = 0.3f,
                    y = 0.4f,
                    scale = 1.2f,
                    color = 0xFFFF69B4,
                    alpha = 0.6f
                ),
            ),
        ),
    ),
    particles = listOf(
        DreamParticle(
            shape = ParticleShape.SPARKLE,
            count = 12,
            color = 0x80FFD700,
            speed = 1.0f,
            size = 4f
        ),
    ),
)

@Preview(name = "Front — Idle (no flip icon)")
@Composable
private fun FrontIdlePreview() {
    SofaAiTheme {
        Surface {
            FlippableDreamCard(
                scene = previewDarkScene(),
                imageState = DreamImageState.Idle,
            )
        }
    }
}

@Preview(name = "Front — Flippable (flip icon visible)")
@Composable
private fun FrontFlippablePreview() {
    SofaAiTheme {
        Surface {
            FlippableDreamCard(
                scene = previewDarkScene(),
                imageState = DreamImageState.Generated(
                    mimeType = "image/png",
                    artistName = "Lorem ipsum"
                ),
            )
        }
    }
}

@Preview(name = "Front — Flippable Dark")
@Composable
private fun FrontFlippableDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            FlippableDreamCard(
                scene = previewWarmScene(),
                imageState = DreamImageState.Generated(
                    mimeType = "image/png",
                    artistName = "Lorem ipsum"
                ),
            )
        }
    }
}

@Preview(name = "Back — Placeholder (dark palette)")
@Composable
private fun BackPlaceholderDarkPalettePreview() {
    SofaAiTheme {
        Surface {
            NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                BackSide(
                    imageState = DreamImageState.Generated(
                        mimeType = "image/png",
                        artistName = "Lorem ipsum"
                    ),
                    scene = previewDarkScene(),
                )
            }
        }
    }
}

@Preview(name = "Back — Placeholder (warm palette)")
@Composable
private fun BackPlaceholderWarmPalettePreview() {
    SofaAiTheme {
        Surface {
            NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                BackSide(
                    imageState = DreamImageState.Generated(
                        mimeType = "image/png",
                        artistName = "Salvador Dalí"
                    ),
                    scene = previewWarmScene(),
                )
            }
        }
    }
}

@Preview(name = "Back — Placeholder Dark Theme")
@Composable
private fun BackPlaceholderDarkThemePreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                BackSide(
                    imageState = DreamImageState.Generated(
                        mimeType = "image/png",
                        artistName = "Claude Monet"
                    ),
                    scene = previewDarkScene(),
                )
            }
        }
    }
}

// endregion
