package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoBrutalTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    NeoBrutalCard(
        // Reusing the Card for a consistent look
        modifier = modifier.fillMaxWidth().padding(horizontal = Dimensions.paddingMedium),
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
            actions = {
                Row(
                    modifier = Modifier.padding(horizontal = Dimensions.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()
                }
            },
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
