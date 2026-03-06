package se.onemanstudio.playaroundwithai.core.tracking.model

data class DailyTokenUsage(
    val dayLabel: String,
    val totalTokens: Long,
    val promptTokens: Long,
    val candidateTokens: Long,
    val callCount: Int,
)
