package se.onemanstudio.playaroundwithai.data.chat.domain.usecase

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.network.tracking.DailyTokenUsage
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageQuery
import javax.inject.Inject

class GetWeeklyTokenUsageUseCase @Inject constructor(
    private val tokenUsageQuery: TokenUsageQuery,
) {
    operator fun invoke(): Flow<List<DailyTokenUsage>> = tokenUsageQuery.getWeeklyUsage()
}
