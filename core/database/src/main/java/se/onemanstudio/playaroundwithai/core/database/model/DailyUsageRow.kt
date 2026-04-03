package se.onemanstudio.playaroundwithai.core.database.model

data class DailyUsageRow(
    val dateMillis: Long,
    val totalTokens: Long,
    val promptTokens: Long,
    val candidateTokens: Long,
    val callCount: Int,
)
