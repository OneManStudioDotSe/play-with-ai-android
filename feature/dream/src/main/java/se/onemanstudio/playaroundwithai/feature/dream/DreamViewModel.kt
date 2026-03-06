package se.onemanstudio.playaroundwithai.feature.dream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.GenerateDreamImageUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.GetDreamHistoryUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.InterpretDreamUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.SaveDreamImageUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.SaveDreamUseCase
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamError
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamImageState
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamScreenState
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamUiState
import timber.log.Timber
import java.io.IOException
import java.time.Instant
import javax.inject.Inject

private const val DREAM_ID_TIMEOUT_MS = 5_000L

@HiltViewModel
class DreamViewModel @Inject constructor(
    private val interpretDreamUseCase: InterpretDreamUseCase,
    private val saveDreamUseCase: SaveDreamUseCase,
    private val getDreamHistoryUseCase: GetDreamHistoryUseCase,
    private val generateDreamImageUseCase: GenerateDreamImageUseCase,
    private val saveDreamImageUseCase: SaveDreamImageUseCase,
    private val apiKeyAvailability: ApiKeyAvailability,
) : ViewModel() {

    private val _screenState = MutableStateFlow(DreamScreenState())
    val screenState = _screenState.asStateFlow()

    private var imageGenerationJob: Job? = null

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

    fun interpretDream(description: String) {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _screenState.update { it.copy(dreamState = DreamUiState.Error(DreamError.ApiKeyMissing)) }
            return
        }

        _screenState.update {
            it.copy(dreamState = DreamUiState.Interpreting, imageState = DreamImageState.Generating, currentDescription = description)
        }

        imageGenerationJob?.cancel()
        val dreamIdDeferred = CompletableDeferred<Long>()
        viewModelScope.launch { runInterpretation(description, dreamIdDeferred) }
        imageGenerationJob = viewModelScope.launch { runImageGeneration(description, dreamIdDeferred) }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun runInterpretation(description: String, dreamIdDeferred: CompletableDeferred<Long>) {
        interpretDreamUseCase(description)
            .onSuccess { interpretation ->
                logSceneSpecs(interpretation.scene)
                _screenState.update {
                    it.copy(
                        dreamState = DreamUiState.Result(
                            interpretation = interpretation.textAnalysis,
                            scene = interpretation.scene,
                            mood = interpretation.mood,
                        ),
                    )
                }

                try {
                    val dreamId = saveDreamUseCase(
                        Dream(
                            description = description,
                            interpretation = interpretation.textAnalysis,
                            scene = interpretation.scene,
                            mood = interpretation.mood,
                            timestamp = Instant.now(),
                        )
                    )
                    dreamIdDeferred.complete(dreamId)
                } catch (e: Exception) {
                    Timber.e(e, "DreamVM - Failed to save dream to local DB")
                    dreamIdDeferred.cancel()
                }
            }
            .onFailure { exception ->
                _screenState.update { it.copy(dreamState = DreamUiState.Error(mapException(exception))) }
                dreamIdDeferred.cancel()
            }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun runImageGeneration(description: String, dreamIdDeferred: CompletableDeferred<Long>) {
        generateDreamImageUseCase(description)
            .onSuccess { dreamImage ->
                _screenState.update {
                    it.copy(
                        imageState = DreamImageState.Generated(
                            imageBase64 = dreamImage.imageBase64,
                            mimeType = dreamImage.mimeType,
                            artistName = dreamImage.artistName,
                        ),
                    )
                }

                try {
                    val dreamId = withTimeoutOrNull(DREAM_ID_TIMEOUT_MS) { dreamIdDeferred.await() }
                    if (dreamId == null) {
                        Timber.w("DreamVM - Timed out waiting for dream ID; image will not be persisted")
                        return@onSuccess
                    }
                    val imageBytes = java.util.Base64.getDecoder().decode(dreamImage.imageBase64)
                    val imagePath = saveDreamImageUseCase(dreamId, imageBytes, dreamImage.mimeType, dreamImage.artistName)
                    _screenState.update { state ->
                        val current = state.imageState
                        if (current is DreamImageState.Generated) {
                            state.copy(imageState = current.copy(imagePath = imagePath))
                        } else {
                            state
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "DreamVM - Failed to persist dream image")
                }
            }
            .onFailure { exception ->
                Timber.e(exception, "DreamVM - Image generation failed")
                _screenState.update {
                    it.copy(imageState = DreamImageState.Error(exception.localizedMessage ?: "Image generation failed"))
                }
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
                ),
                currentDescription = dream.description,
                imageState = if (dream.imagePath != null) {
                    DreamImageState.Generated(
                        imagePath = dream.imagePath,
                        mimeType = "image/png",
                        artistName = dream.artistName ?: "",
                    )
                } else {
                    DreamImageState.Idle
                },
            )
        }
    }

    fun clearResult() {
        imageGenerationJob?.cancel()
        _screenState.update { it.copy(dreamState = DreamUiState.Initial, imageState = DreamImageState.Idle, currentDescription = "") }
    }

    private fun mapException(exception: Throwable): DreamError = when (exception) {
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

    @Suppress("MagicNumber")
    private fun logSceneSpecs(scene: DreamScene) {
        fun colorHex(c: Long) = "0x${c.toInt().toUInt().toString(16).uppercase().padStart(8, '0')}"

        val sb = StringBuilder()
        sb.appendLine("=== Dream Scene Specs ===")
        sb.appendLine("Palette: sky=${colorHex(scene.palette.sky)}, horizon=${colorHex(scene.palette.horizon)}, accent=${colorHex(scene.palette.accent)}")
        scene.layers.forEachIndexed { i, layer ->
            sb.appendLine("Layer $i (depth=${layer.depth}):")
            layer.elements.forEach { e ->
                sb.appendLine("  ${e.shape} at (${e.x}, ${e.y}) scale=${e.scale} color=${colorHex(e.color)} alpha=${e.alpha}")
            }
        }
        scene.particles.forEach { p ->
            sb.appendLine("Particles: ${p.count}x ${p.shape} color=${colorHex(p.color)} speed=${p.speed} size=${p.size}")
        }
        Timber.d(sb.toString())
    }
}
