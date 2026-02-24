package se.onemanstudio.playaroundwithai.feature.chat.views.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.feature.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.feature.chat.R

private val DragHandleWidth = 32.dp
private val DragHandleHeight = 4.dp
private val DragHandleCornerRadius = 2.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    history: List<Prompt>,
    onDismissRequest: () -> Unit,
    onHistoryItemClick: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(Dimensions.paddingMedium),
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                // a custom drag handle to match our theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimensions.paddingMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(DragHandleWidth)
                            .height(DragHandleHeight)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium),
                                shape = RoundedCornerShape(DragHandleCornerRadius)
                            )
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.paddingLarge),
                    contentPadding = PaddingValues(
                        top = Dimensions.paddingLarge,
                        bottom = Dimensions.paddingExtraLarge
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.prompt_history),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Dimensions.paddingMedium)
                        )
                    }

                    if (history.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_history_yet),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimensions.paddingExtraLarge)
                            )
                        }
                    } else {
                        items(history) { prompt ->
                            HistoryItemCard(
                                prompt = prompt,
                                onClick = onHistoryItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}
