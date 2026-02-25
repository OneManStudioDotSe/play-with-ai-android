package se.onemanstudio.playaroundwithai.core.network.tracking

import kotlinx.coroutines.flow.SharedFlow
import se.onemanstudio.playaroundwithai.core.network.dto.UsageMetadata

data class TokenUsageEvent(val feature: String, val totalTokens: Int)

interface TokenUsageTracker {
    val lastUsageEvent: SharedFlow<TokenUsageEvent>
    suspend fun record(feature: String, usageMetadata: UsageMetadata?)
}
