package se.onemanstudio.playaroundwithai.core.network.tracking

import kotlinx.coroutines.flow.Flow

data class DailyTokenUsage(
    val dayLabel: String,
    val totalTokens: Long,
    val promptTokens: Long,
    val candidateTokens: Long,
    val callCount: Int,
)

interface TokenUsageQuery {
    fun getWeeklyUsage(): Flow<List<DailyTokenUsage>>
}
