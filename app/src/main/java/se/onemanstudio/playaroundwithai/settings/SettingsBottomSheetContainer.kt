package se.onemanstudio.playaroundwithai.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.onemanstudio.playaroundwithai.core.ui.sofa.ChartBarData
import se.onemanstudio.playaroundwithai.core.ui.sofa.SettingsBottomSheet
import se.onemanstudio.playaroundwithai.core.ui.sofa.SettingsState
import java.text.NumberFormat
import java.util.Locale
import androidx.core.net.toUri

@Composable
fun SettingsBottomSheetContainer(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val weeklyUsage by viewModel.weeklyUsage.collectAsStateWithLifecycle()
    val selectedDayIndex by viewModel.selectedDayIndex.collectAsStateWithLifecycle()
    val vehicleCount by viewModel.vehicleCount.collectAsStateWithLifecycle()
    val searchRadiusKm by viewModel.searchRadiusKm.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    val usageBars = weeklyUsage.map { day ->
        ChartBarData(
            label = day.dayLabel,
            value = day.totalTokens.toFloat(),
            detailText = "${day.dayLabel}: ${numberFormat.format(day.totalTokens)} tokens (${day.callCount} calls)",
        )
    }

    SettingsBottomSheet(
        state = SettingsState(
            appVersion = "1.0",
            vehicleCount = vehicleCount,
            searchRadiusKm = searchRadiusKm,
        ),
        onDismiss = onDismiss,
        onVehicleCountChange = { viewModel.onVehicleCountChange(it) },
        onSearchRadiusChange = { viewModel.onSearchRadiusChange(it) },
        onContactClick = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:sotiris@onemanstudio.se".toUri()
            }
            context.startActivity(intent)
        },
        usageBars = usageBars,
        selectedDayIndex = selectedDayIndex,
        onBarTapped = { viewModel.onBarTapped(it) },
    )
}
