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
sealed interface DreamImageState {
    data object Idle : DreamImageState
    data object Generating : DreamImageState
    data class Generated(
        val imagePath: String? = null,
        val imageBase64: String? = null,
        val mimeType: String,
        val artistName: String,
    ) : DreamImageState
    data class Error(val message: String) : DreamImageState
}

@Immutable
data class DreamScreenState(
    val dreamState: DreamUiState = DreamUiState.Initial,
    val dreamHistory: List<Dream> = emptyList(),
    val imageState: DreamImageState = DreamImageState.Idle,
    val currentDescription: String = "",
)
