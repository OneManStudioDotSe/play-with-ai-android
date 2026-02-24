package se.onemanstudio.playaroundwithai.feature.dream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.DeleteDreamUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.GetDreamHistoryUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.InterpretDreamUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.SaveDreamUseCase
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamError
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamScreenState
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamUiState
import timber.log.Timber
import java.io.IOException
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DreamViewModel @Inject constructor(
    private val interpretDreamUseCase: InterpretDreamUseCase,
    private val saveDreamUseCase: SaveDreamUseCase,
    private val deleteDreamUseCase: DeleteDreamUseCase,
    getDreamHistoryUseCase: GetDreamHistoryUseCase,
    private val apiKeyAvailability: ApiKeyAvailability,
) : ViewModel() {

    private val _screenState = MutableStateFlow(DreamScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _screenState.update { it.copy(dreamState = DreamUiState.Error(DreamError.ApiKeyMissing)) }
        }

        observeDreamHistory(getDreamHistoryUseCase)
    }

    private fun observeDreamHistory(getDreamHistoryUseCase: GetDreamHistoryUseCase) {
        viewModelScope.launch {
            getDreamHistoryUseCase().collect { history ->
                _screenState.update { it.copy(dreamHistory = history) }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun interpretDream(description: String) {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _screenState.update { it.copy(dreamState = DreamUiState.Error(DreamError.ApiKeyMissing)) }
            return
        }

        _screenState.update { it.copy(dreamState = DreamUiState.Interpreting) }

        viewModelScope.launch {
            interpretDreamUseCase(description)
                .onSuccess { interpretation ->
                    _screenState.update {
                        it.copy(
                            dreamState = DreamUiState.Result(
                                interpretation = interpretation.textAnalysis,
                                scene = interpretation.scene,
                                mood = interpretation.mood,
                            )
                        )
                    }

                    try {
                        saveDreamUseCase(
                            Dream(
                                description = description,
                                interpretation = interpretation.textAnalysis,
                                scene = interpretation.scene,
                                mood = interpretation.mood,
                                timestamp = Instant.now(),
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "DreamVM - Failed to save dream to local DB")
                    }
                }
                .onFailure { exception ->
                    val error = when (exception) {
                        is IOException -> DreamError.NetworkMissing
                        is IllegalArgumentException -> {
                            if (exception.message?.contains("maximum length") == true) {
                                DreamError.InputTooLong
                            } else {
                                DreamError.Unknown(exception.localizedMessage)
                            }
                        }
                        else -> DreamError.Unknown(exception.localizedMessage)
                    }
                    _screenState.update { it.copy(dreamState = DreamUiState.Error(error)) }
                }
        }
    }

    fun deleteDream(id: Long) {
        viewModelScope.launch {
            deleteDreamUseCase(id)
        }
    }

    fun restoreDream(dream: Dream) {
        val scene = dream.scene ?: return
        _screenState.update {
            it.copy(
                dreamState = DreamUiState.Result(
                    interpretation = dream.interpretation,
                    scene = scene,
                    mood = dream.mood,
                )
            )
        }
    }

    fun clearResult() {
        _screenState.update { it.copy(dreamState = DreamUiState.Initial) }
    }
}
