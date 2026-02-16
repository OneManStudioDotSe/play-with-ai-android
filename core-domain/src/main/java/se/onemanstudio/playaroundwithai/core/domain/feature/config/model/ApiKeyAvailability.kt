package se.onemanstudio.playaroundwithai.core.domain.feature.config.model

data class ApiKeyAvailability(
    val isGeminiKeyAvailable: Boolean,
    val isMapsKeyAvailable: Boolean,
)
