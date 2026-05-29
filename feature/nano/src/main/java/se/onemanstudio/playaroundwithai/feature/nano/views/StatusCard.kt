package se.onemanstudio.playaroundwithai.feature.nano.views

import androidx.compose.runtime.Composable
import se.onemanstudio.playaroundwithai.feature.nano.states.NanoPhase

@Composable
fun StatusCard(
    phase: NanoPhase,
    onDownload: () -> Unit,
    onRetry: () -> Unit,
) {
    when (phase) {
        NanoPhase.Checking -> CheckingCard()
        is NanoPhase.Downloading -> DownloadingCard(phase)
        is NanoPhase.Error -> ErrorCard(message = phase.message, onRetry = onRetry)
        is NanoPhase.Evaluated -> EvaluatedCard(support = phase.support, onDownload = onDownload)
    }
}
