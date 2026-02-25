package se.onemanstudio.playaroundwithai.feature.dream.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
private const val CARD_WIDTH = 180
private const val DESCRIPTION_MAX_LINES = 4
private const val EMOJI_BACKGROUND_ALPHA = 0.5f

@Composable
fun DreamGalleryRow(
    modifier: Modifier = Modifier,
    dreams: List<Dream>,
    onDreamClick: (Dream) -> Unit,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
    ) {
        items(dreams, key = { it.id }) { dream ->
            DreamGalleryCard(
                dream = dream,
                onClick = { onDreamClick(dream) },
            )
        }
    }
}

@Composable
private fun DreamGalleryCard(
    dream: Dream,
    onClick: () -> Unit,
) {
    NeoBrutalCard(
        modifier = Modifier
            .width(CARD_WIDTH.dp)
            .clickable(onClick = onClick),
    ) {
        Box {
            Text(
                text = moodToEmoji(dream.mood),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(EMOJI_BACKGROUND_ALPHA),
            )

            Column(modifier = Modifier.padding(Dimensions.paddingMedium)) {
                Text(
                    text = dream.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = DESCRIPTION_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                Text(
                    text = dream.timestamp.atZone(ZoneId.systemDefault()).format(dateFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = se.onemanstudio.playaroundwithai.core.ui.theme.Alphas.medium),
                )
            }
        }
    }
}

private fun moodToEmoji(mood: DreamMood): String = when (mood) {
    DreamMood.JOYFUL -> "\u2600\uFE0F"
    DreamMood.MYSTERIOUS -> "\uD83C\uDF19"
    DreamMood.ANXIOUS -> "\u26A1"
    DreamMood.PEACEFUL -> "\uD83C\uDF3F"
    DreamMood.DARK -> "\uD83C\uDF11"
    DreamMood.SURREAL -> "\uD83C\uDF00"
}

@Suppress("MagicNumber")
private fun previewDreams() = listOf(
    Dream(id = 1, description = "Flying over a purple ocean under two moons", mood = DreamMood.SURREAL, timestamp = Instant.now()),
    Dream(id = 2, description = "Walking through a forest of glass trees", mood = DreamMood.MYSTERIOUS, timestamp = Instant.now()),
    Dream(id = 3, description = "A sunny meadow reunion", mood = DreamMood.JOYFUL, timestamp = Instant.now()),
    Dream(id = 4, description = "Falling through endless clouds", mood = DreamMood.ANXIOUS, timestamp = Instant.now()),
)

@Preview(name = "Light")
@Composable
private fun DreamGalleryRowLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            DreamGalleryRow(dreams = previewDreams(), onDreamClick = {})
        }
    }
}

@Preview(name = "Dark")
@Composable
private fun DreamGalleryRowDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            DreamGalleryRow(dreams = previewDreams(), onDreamClick = {})
        }
    }
}
