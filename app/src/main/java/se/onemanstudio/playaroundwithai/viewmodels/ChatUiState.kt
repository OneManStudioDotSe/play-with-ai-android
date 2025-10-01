package se.onemanstudio.playaroundwithai.viewmodels

sealed interface ChatUiState {
    data object Initial : ChatUiState
    data object Loading : ChatUiState
    data class Success(val outputText: String) : ChatUiState
    data class Error(val errorMessage: String) : ChatUiState
}