package se.onemanstudio.playaroundwithai.feature.chat.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import se.onemanstudio.playaroundwithai.core.data.domain.model.Prompt
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.views.HistoryItemCard

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
    ) {
        NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.paddingLarge),
                contentPadding = PaddingValues(
                    top = Dimensions.paddingLarge,
                    bottom = Dimensions.paddingExtraLarge
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)
            ) {
                item {
                    Text(
                        text = "Prompt history",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = Dimensions.paddingMedium)
                    )
                }
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
