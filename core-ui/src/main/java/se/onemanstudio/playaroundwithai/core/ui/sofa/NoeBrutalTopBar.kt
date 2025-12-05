package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoBrutalTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    NeoBrutalCard(
        // Reusing the Card for a consistent look
        modifier = modifier.fillMaxWidth(),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            actions = actions,
            // Make TopAppBar transparent to let NeoBrutalCard handle the drawing
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
        )
    }
}

@Preview(name = "Top App Bar - Light Theme", showBackground = true, backgroundColor = 0xFFF8F8F8)
@Composable
private fun NeoBrutalTopAppBarPreview_Light() {
    SofaAiTheme {
        Column {
            NeoBrutalTopAppBar(
                title = "PREVIEW TITLE",
                actions = {
                    NeoBrutalIconButton(
                        onClick = {},
                        imageVector = Icons.Default.History,
                        contentDescription = "History"
                    )
                }
            )
        }
    }
}

@Preview(name = "Top App Bar - Dark Theme", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun NeoBrutalTopAppBarPreview_Dark() {
    SofaAiTheme(darkTheme = true) {
        Column {
            NeoBrutalTopAppBar(
                title = "PREVIEW TITLE",
                actions = {
                    NeoBrutalIconButton(
                        onClick = {},
                        imageVector = Icons.Default.History,
                        contentDescription = "History"
                    )
                }
            )
        }
    }
}
