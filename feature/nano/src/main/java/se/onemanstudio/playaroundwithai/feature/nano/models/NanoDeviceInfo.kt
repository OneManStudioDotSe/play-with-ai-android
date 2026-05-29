package se.onemanstudio.playaroundwithai.feature.nano.models

import android.os.Build

/** Static, human-readable facts about the current device, shown for context. */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidApiLevel: Int,
) {
    companion object {
        fun current(): DeviceInfo = DeviceInfo(
            manufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase() },
            model = Build.MODEL.orEmpty(),
            androidApiLevel = Build.VERSION.SDK_INT,
        )
    }
}

/**
 * The three possible outcomes of evaluating on-device AI (Gemini Nano) support,
 * mirroring ML Kit's [com.google.mlkit.genai.common.FeatureStatus].
 */
enum class NanoSupport {
    /** Device cannot run Gemini Nano. */
    NOT_AVAILABLE,

    /** Supported, but the model still needs to be downloaded. */
    DOWNLOADABLE,

    /** Model is downloaded and ready to use. */
    READY,
}
