package se.onemanstudio.playaroundwithai.feature.chat.states

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SnackbarEvent {
    data object LocalSaveFailed : SnackbarEvent
    data object LocalUpdateFailed : SnackbarEvent
    data class SyncFailed(val failedCount: Int) : SnackbarEvent
}
