package se.onemanstudio.playaroundwithai.feature.chat.states

sealed interface ChatUiState {
    data object Initial : ChatUiState
    data object Loading : ChatUiState
    data class Success(val outputText: String) : ChatUiState
    data class Error(val error: ChatError) : ChatUiState
}

sealed interface ChatError {
    data object NetworkMissing : ChatError
    data object Permission : ChatError
    data object FileNotFound : ChatError
    data object FileRead : ChatError
    data class Unknown(val message: String?) : ChatError
}
