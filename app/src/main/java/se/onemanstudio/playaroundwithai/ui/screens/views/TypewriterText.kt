package se.onemanstudio.playaroundwithai.ui.screens.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

const val typingDelay = 20L

@Composable
fun TypewriterText(text: String) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(key1 = text) {
        displayedText = ""
        text.forEach { char ->
            displayedText += char
            delay(typingDelay)
        }
    }

    Text(
        text = displayedText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

/**
 * Static preview to check the final style, font, and color of the text.
 * The animation itself won't play here.
 */
@Preview(showBackground = true, name = "Static Style Preview")
@Composable
private fun TypewriterText_StaticPreview() {
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
private fun TypewriterText_InteractivePreview() {
    SofaAiTheme {
        Surface {
            TypewriterText(text = "Hello, this text will type out one letter at a time...")
        }
    }
}
