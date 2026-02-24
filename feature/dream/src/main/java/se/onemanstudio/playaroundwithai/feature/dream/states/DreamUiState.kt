package se.onemanstudio.playaroundwithai.feature.dream.states

import androidx.compose.runtime.Immutable
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene

@Immutable
sealed interface DreamUiState {
    data object Initial : DreamUiState
    data object Interpreting : DreamUiState
    data class Result(
        val interpretation: String,
        val scene: DreamScene,
        val mood: DreamMood,
    ) : DreamUiState
    data class Error(val error: DreamError) : DreamUiState
}

@Immutable
sealed interface DreamError {
    data object ApiKeyMissing : DreamError
    data object NetworkMissing : DreamError
    data object InputTooLong : DreamError
    data class Unknown(val message: String?) : DreamError
}

@Immutable
data class DreamScreenState(
    val dreamState: DreamUiState = DreamUiState.Initial,
    val dreamHistory: List<Dream> = emptyList(),
)
