package se.onemanstudio.playaroundwithai.feature.settings

import androidx.compose.runtime.Immutable
import se.onemanstudio.playaroundwithai.core.config.settings.AiPersona

@Immutable
data class SettingsState(
    val appVersion: String,
    val showTokenUsage: Boolean = false,
    val vehicleCount: Int = DEFAULT_VEHICLE_COUNT,
    val searchRadiusKm: Float = DEFAULT_SEARCH_RADIUS_KM,
    val walkingSpeedKmh: Float = WALKING_SPEED_NORMAL,
    val typewriterDelayMs: Long = TYPEWRITER_DELAY_NORMAL,
    val hapticFeedbackEnabled: Boolean = true,
    val networkTimeoutSeconds: Int = DEFAULT_NETWORK_TIMEOUT_SECONDS,
    val tokenTrackingEnabled: Boolean = true,
    val tripLengthMinStops: Int = TRIP_LENGTH_STANDARD_MIN,
    val firebaseSyncEnabled: Boolean = true,
    val imageQualityJpeg: Int = IMAGE_QUALITY_MEDIUM,
    val agentMaxIterations: Int = AGENT_ITERATIONS_STANDARD,
    val suggestedPlacesCount: Int = DEFAULT_SUGGESTED_PLACES_COUNT,
    val maxSelectablePoints: Int = DEFAULT_MAX_SELECTABLE_POINTS,
    val geminiTextModel: String = GEMINI_TEXT_MODEL_DEFAULT,
    val geminiImageModel: String = GEMINI_IMAGE_MODEL_DEFAULT,
    val aiPersona: AiPersona = AiPersona.AI_OVERLORD,
) {
    companion object {
        const val MIN_VEHICLE_COUNT = 10
        const val MAX_VEHICLE_COUNT = 100
        const val DEFAULT_VEHICLE_COUNT = 30
        const val MIN_SEARCH_RADIUS_KM = 1.0f
        const val MAX_SEARCH_RADIUS_KM = 10.0f
        const val DEFAULT_SEARCH_RADIUS_KM = 4.0f
        const val WALKING_SPEED_SLOW = 3.0f
        const val WALKING_SPEED_NORMAL = 5.0f
        const val WALKING_SPEED_FAST = 7.0f
        const val TYPEWRITER_DELAY_INSTANT = 0L
        const val TYPEWRITER_DELAY_FAST = 5L
        const val TYPEWRITER_DELAY_NORMAL = 10L
        const val TYPEWRITER_DELAY_SLOW = 30L
        const val MIN_NETWORK_TIMEOUT_SECONDS = 15
        const val MAX_NETWORK_TIMEOUT_SECONDS = 120
        const val DEFAULT_NETWORK_TIMEOUT_SECONDS = 30
        const val NETWORK_TIMEOUT_STEP_SECONDS = 15
        const val TRIP_LENGTH_QUICK_MIN = 2
        const val TRIP_LENGTH_STANDARD_MIN = 4
        const val TRIP_LENGTH_EXTENDED_MIN = 7
        const val IMAGE_QUALITY_LOW = 40
        const val IMAGE_QUALITY_MEDIUM = 77
        const val IMAGE_QUALITY_HIGH = 93
        const val AGENT_ITERATIONS_QUICK = 5
        const val AGENT_ITERATIONS_STANDARD = 10
        const val AGENT_ITERATIONS_THOROUGH = 15
        const val MIN_SUGGESTED_PLACES_COUNT = 5
        const val MAX_SUGGESTED_PLACES_COUNT = 20
        const val DEFAULT_SUGGESTED_PLACES_COUNT = 10
        const val SUGGESTED_PLACES_COUNT_STEP = 5
        const val MIN_MAX_SELECTABLE_POINTS = 3
        const val MAX_MAX_SELECTABLE_POINTS = 12
        const val DEFAULT_MAX_SELECTABLE_POINTS = 8
        const val GEMINI_TEXT_MODEL_DEFAULT = "gemini-3-flash-preview"
        const val GEMINI_TEXT_MODEL_FLASH_25 = "gemini-2.5-flash"
        const val GEMINI_TEXT_MODEL_FLASH_25_LITE = "gemini-2.5-flash-lite"
        const val GEMINI_TEXT_MODEL_PRO_25 = "gemini-2.5-pro"
        const val GEMINI_TEXT_MODEL_PRO_31_PREVIEW = "gemini-3.1-pro-preview"
        const val GEMINI_IMAGE_MODEL_DEFAULT = "gemini-2.5-flash-image"
        const val GEMINI_IMAGE_MODEL_FLASH_31_PREVIEW = "gemini-3.1-flash-image-preview"
        const val GEMINI_IMAGE_MODEL_PRO_3_PREVIEW = "gemini-3-pro-image-preview"
    }
}
