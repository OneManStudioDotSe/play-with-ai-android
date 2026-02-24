package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoBrutalTopAppBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
            )
        },
        actions = {
            Row(
                modifier = Modifier.padding(horizontal = Dimensions.paddingLarge),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
    )
}

@Preview(name = "Light")
@Composable
private fun NeoBrutalTopAppBarLightPreview() {
    SofaAiTheme {
        Column {
            NeoBrutalTopAppBar(
                title = "Some title",
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

@Preview(name = "Dark")
@Composable
private fun NeoBrutalTopAppBarDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Column {
            NeoBrutalTopAppBar(
                title = "Some title",
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
