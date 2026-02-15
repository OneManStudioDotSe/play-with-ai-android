package se.onemanstudio.playaroundwithai.feature.chat.models

sealed interface SnackbarEvent {
    data class LocalSaveFailed(val message: String) : SnackbarEvent
    data class LocalUpdateFailed(val message: String) : SnackbarEvent
    data class SyncFailed(val failedCount: Int) : SnackbarEvent
}
