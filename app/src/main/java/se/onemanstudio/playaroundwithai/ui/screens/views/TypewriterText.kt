package se.onemanstudio.playaroundwithai.ui.screens.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

// Add this composable to the bottom of ChatScreen.kt
@Composable
fun TypewriterText(text: String) {
    var displayedText by remember { mutableStateOf("") }

    // This LaunchedEffect will re-launch whenever the underlying `text` changes
    LaunchedEffect(key1 = text) {
        displayedText = "" // Reset the text for a new response
        text.forEach { char ->
            displayedText += char
            delay(20L) // Adjust the delay to change the typing speed
        }
    }

    Text(
        text = displayedText,
        style = MaterialTheme.typography.bodyLarge
    )
}
