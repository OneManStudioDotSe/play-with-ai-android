package se.onemanstudio.playaroundwithai.feature.nano

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.Summarizer
import com.google.mlkit.genai.summarization.SummarizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.feature.nano.models.DeviceInfo
import se.onemanstudio.playaroundwithai.feature.nano.models.NanoSupport
import se.onemanstudio.playaroundwithai.feature.nano.states.NanoPhase
import se.onemanstudio.playaroundwithai.feature.nano.states.NanoUiState
import timber.log.Timber
import javax.inject.Inject

/**
 * Evaluates on-device AI (Gemini Nano) support through ML Kit's GenAI APIs.
 *
 * ML Kit has no standalone "is Nano available" call — every GenAI feature is
 * backed by Gemini Nano, so we use the Summarization client purely as a probe:
 * [Summarizer.checkFeatureStatus] returns the definitive runtime status, and
 * [Summarizer.downloadFeature] pulls the model when it is only downloadable.
 */
@HiltViewModel
class NanoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(NanoUiState(device = DeviceInfo.current(), phase = NanoPhase.Checking))
    val state: StateFlow<NanoUiState> = _state.asStateFlow()

    private val summarizer: Summarizer by lazy {
        val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.ARTICLE)
            .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
            .setLanguage(SummarizerOptions.Language.ENGLISH)
            .build()
        Summarization.getClient(options)
    }

    init {
        checkSupport()
    }

    fun checkSupport() {
        _state.update { it.copy(phase = NanoPhase.Checking) }
        viewModelScope.launch {
            runCatching { summarizer.checkFeatureStatus().await() }
                .onSuccess { status -> _state.update { it.copy(phase = NanoPhase.Evaluated(status.toSupport())) } }
                .onFailure { error -> onError(error) }
        }
    }

    fun downloadModel() {
        _state.update { it.copy(phase = NanoPhase.Downloading(downloadedBytes = 0L, totalBytes = 0L)) }
        summarizer.downloadFeature(object : DownloadCallback {
            override fun onDownloadStarted(bytesToDownload: Long) {
                _state.update { it.copy(phase = NanoPhase.Downloading(downloadedBytes = 0L, totalBytes = bytesToDownload)) }
            }

            override fun onDownloadProgress(totalBytesDownloaded: Long) {
                _state.update { current ->
                    val total = (current.phase as? NanoPhase.Downloading)?.totalBytes ?: 0L
                    current.copy(phase = NanoPhase.Downloading(downloadedBytes = totalBytesDownloaded, totalBytes = total))
                }
            }

            override fun onDownloadCompleted() {
                // Re-run the evaluation so the UI reflects the now-ready model.
                checkSupport()
            }

            override fun onDownloadFailed(e: GenAiException) {
                onError(e)
            }
        })
    }

    private fun onError(throwable: Throwable) {
        Timber.w(throwable, "Gemini Nano feature check/download failed")
        _state.update { it.copy(phase = NanoPhase.Error(throwable.message.orEmpty())) }
    }

    private fun Int.toSupport(): NanoSupport = when (this) {
        FeatureStatus.AVAILABLE -> NanoSupport.READY
        FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> NanoSupport.DOWNLOADABLE
        else -> NanoSupport.NOT_AVAILABLE
    }

    override fun onCleared() {
        super.onCleared()
        runCatching { summarizer.close() }
    }
}
