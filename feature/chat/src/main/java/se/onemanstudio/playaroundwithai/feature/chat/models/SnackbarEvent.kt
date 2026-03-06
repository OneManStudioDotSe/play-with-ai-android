package se.onemanstudio.playaroundwithai.feature.chat.models

@androidx.compose.runtime.Immutable
sealed interface SnackbarEvent {
    data object LocalSaveFailed : SnackbarEvent
    data object LocalUpdateFailed : SnackbarEvent
    data class SyncFailed(val failedCount: Int) : SnackbarEvent
}
