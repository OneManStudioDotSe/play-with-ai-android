package se.onemanstudio.playaroundwithai.ui.screens.unused

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun DesignDemoScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Typography
        item { ComponentGroup(title = "TYPOGRAPHY") {
            Text("Display Large", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Headline Large", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Title Large", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Body Large (Default)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Label Large (Buttons)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
        }}

        // Buttons
        item { ComponentGroup(title = "BUTTONS") {
            NeoBrutalButton(onClick = {}, text = "PRIMARY ACTION", backgroundColor = MaterialTheme.colorScheme.primary)
            NeoBrutalButton(onClick = {}, text = "SECONDARY ACTION", backgroundColor = MaterialTheme.colorScheme.secondary)
            NeoBrutalButton(onClick = {}, text = "TERTIARY ACTION", backgroundColor = MaterialTheme.colorScheme.tertiary)
        }}

        // Cards
        item { ComponentGroup(title = "CARDS") {
            NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Card Title", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This is a card. It uses the neoBrutalism modifier to get a background, border, and shadow.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }}

        // Add more components: TextFields, etc.
    }
}

@Composable
private fun ComponentGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "--- $title ---",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun DesignDemoScreenPreview_Light() {
    SofaAiTheme {
        DesignDemoScreen()
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DesignDemoScreenPreview_Dark() {
    SofaAiTheme(darkTheme = true) {
        DesignDemoScreen()
    }
}
