package se.onemanstudio.playaroundwithai.feature.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.onemanstudio.playaroundwithai.core.ui.sofa.ChartBarData
import android.content.ActivityNotFoundException
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLocale
import java.text.NumberFormat
import androidx.core.net.toUri
import timber.log.Timber

@Composable
fun SettingsBottomSheetContainer(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val weeklyUsage by viewModel.weeklyUsage.collectAsStateWithLifecycle()
    val selectedDayIndex by viewModel.selectedDayIndex.collectAsStateWithLifecycle()
    val showTokenUsage by viewModel.showTokenUsage.collectAsStateWithLifecycle()
    val vehicleCount by viewModel.vehicleCount.collectAsStateWithLifecycle()
    val searchRadiusKm by viewModel.searchRadiusKm.collectAsStateWithLifecycle()
    val walkingSpeedKmh by viewModel.walkingSpeedKmh.collectAsStateWithLifecycle()
    val typingSpeedDelayMs by viewModel.typingSpeedDelayMs.collectAsStateWithLifecycle()
    val selectedPersona by viewModel.selectedPersona.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val numberFormat = remember(locale) { NumberFormat.getNumberInstance(locale) }

    val usageBars = weeklyUsage.map { day ->
        ChartBarData(
            label = day.dayLabel,
            value = day.totalTokens.toFloat(),
            detailText = "${day.dayLabel}: ${numberFormat.format(day.totalTokens)}/${day.callCount}",
        )
    }

    SettingsBottomSheet(
        state = SettingsState(
            appVersion = viewModel.appVersion,
            showTokenUsage = showTokenUsage,
            vehicleCount = vehicleCount,
            searchRadiusKm = searchRadiusKm,
            walkingSpeedKmh = walkingSpeedKmh,
            typingSpeedDelayMs = typingSpeedDelayMs,
            selectedPersona = selectedPersona,
        ),
        onDismiss = onDismiss,
        onShowTokenUsageChange = { viewModel.onShowTokenUsageChange(it) },
        onVehicleCountChange = { viewModel.onVehicleCountChange(it) },
        onSearchRadiusChange = { viewModel.onSearchRadiusChange(it) },
        onWalkingSpeedChange = { viewModel.onWalkingSpeedChange(it) },
        onTypingSpeedChange = { viewModel.onTypingSpeedChange(it) },
        onPersonaChange = { viewModel.onPersonaChange(it) },
        onContactClick = {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:sotiris@onemanstudio.se".toUri()
                }
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Timber.w(e, "No email app available")
            }
        },
        onLinkedInClick = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, "https://www.linkedin.com/in/sotirisfalieris/".toUri())
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Timber.w(e, "No browser available")
            }
        },
        usageBars = usageBars,
        selectedDayIndex = selectedDayIndex,
        onBarTapped = { viewModel.onBarTapped(it) },
    )
}
