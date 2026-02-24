package se.onemanstudio.playaroundwithai.feature.chat.models

sealed interface SnackbarEvent {
    data object LocalSaveFailed : SnackbarEvent
    data object LocalUpdateFailed : SnackbarEvent
    data class SyncFailed(val failedCount: Int) : SnackbarEvent
}
