package se.onemanstudio.playaroundwithai.feature.chat.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

const val TYPING_DELAY = 10L

@Composable
fun TypewriterText(
    modifier: Modifier = Modifier,
    text: String,
    scrollState: ScrollState? = null
) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(key1 = text) {
        displayedText = ""
        text.forEach { char ->
            val wasAtBottom = scrollState?.let { 
                // Using a small threshold (e.g., 10px) to handle precision issues
                it.value >= it.maxValue - 10 
            } ?: false

            displayedText += char
            delay(TYPING_DELAY)
            
            // Only auto-scroll if we were already at the bottom or if it's the very first scroll
            if (scrollState != null && (wasAtBottom || scrollState.maxValue == 0)) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    Text(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        text = displayedText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Preview(showBackground = true, name = "Static Style Preview")
@Composable
internal fun TypewriterText_StaticPreview() {
    SofaAiTheme {
        Surface {
            Text(
                text = "This is what the final text will look like after the animation is complete.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Interactive preview to see the animation in action.
 * Click the "Start Interactive Mode" button in the Android Studio preview pane to run it.
 */
@Preview(showBackground = true, name = "Interactive Animation Preview")
@Composable
internal fun TypewriterText_InteractivePreview() {
    SofaAiTheme {
        Surface {
            TypewriterText(text = "Hello, this text will type out one letter at a time...")
        }
    }
}
