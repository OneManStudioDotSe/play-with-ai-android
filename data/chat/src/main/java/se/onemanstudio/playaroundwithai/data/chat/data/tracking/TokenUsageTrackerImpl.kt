package se.onemanstudio.playaroundwithai.data.chat.data.tracking

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.core.network.dto.UsageMetadata
import se.onemanstudio.playaroundwithai.core.network.tracking.DailyTokenUsage
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageEvent
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageQuery
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.data.chat.data.local.dao.TokenUsageDao
import se.onemanstudio.playaroundwithai.data.chat.data.local.entity.TokenUsageEntity
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val WEEKLY_DAYS = 7

@Singleton
class TokenUsageTrackerImpl @Inject constructor(
    private val dao: TokenUsageDao,
) : TokenUsageTracker, TokenUsageQuery {

    private val _lastUsageEvent = MutableSharedFlow<TokenUsageEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val lastUsageEvent: SharedFlow<TokenUsageEvent> = _lastUsageEvent.asSharedFlow()

    override suspend fun record(feature: String, usageMetadata: UsageMetadata?) {
        if (usageMetadata == null || usageMetadata.totalTokenCount == 0) return

        val now = System.currentTimeMillis()
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val entity = TokenUsageEntity(
            feature = feature,
            promptTokens = usageMetadata.promptTokenCount,
            candidateTokens = usageMetadata.candidatesTokenCount,
            totalTokens = usageMetadata.totalTokenCount,
            timestamp = now,
            dateMillis = startOfDay,
        )

        dao.insert(entity)
        _lastUsageEvent.tryEmit(TokenUsageEvent(feature, usageMetadata.totalTokenCount))
        Timber.d("TokenUsage - Recorded $feature: ${usageMetadata.totalTokenCount} tokens")
    }

    override fun getWeeklyUsage(): Flow<List<DailyTokenUsage>> {
        val today = LocalDate.now()
        val weekAgo = today.minusDays((WEEKLY_DAYS - 1).toLong())
        val startMillis = weekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return dao.getDailyUsageSince(startMillis).map { rows ->
            val rowMap = rows.associateBy { it.dateMillis }

            (0 until WEEKLY_DAYS).map { offset ->
                val date = weekAgo.plusDays(offset.toLong())
                val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val row = rowMap[millis]

                DailyTokenUsage(
                    dayLabel = label,
                    totalTokens = row?.totalTokens ?: 0L,
                    promptTokens = row?.promptTokens ?: 0L,
                    candidateTokens = row?.candidateTokens ?: 0L,
                    callCount = row?.callCount ?: 0,
                )
            }
        }
    }
}
