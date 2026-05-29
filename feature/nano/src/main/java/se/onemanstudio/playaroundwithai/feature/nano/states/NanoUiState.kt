package se.onemanstudio.playaroundwithai.feature.nano.states

import androidx.compose.runtime.Immutable
import se.onemanstudio.playaroundwithai.feature.nano.models.DeviceInfo
import se.onemanstudio.playaroundwithai.feature.nano.models.NanoSupport

@Immutable
data class NanoUiState(
    val device: DeviceInfo,
    val phase: NanoPhase,
)

/** Transient phases the screen moves through while probing and downloading the model. */
@Immutable
sealed interface NanoPhase {

    /** Asking ML Kit for the current feature status. */
    data object Checking : NanoPhase

    /** ML Kit returned a definitive support verdict (the three evaluation states). */
    data class Evaluated(val support: NanoSupport) : NanoPhase

    /** The model is being downloaded; [totalBytes] is 0 until ML Kit reports the size. */
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long) : NanoPhase

    /** A status check or download failed. */
    data class Error(val message: String) : NanoPhase
}
