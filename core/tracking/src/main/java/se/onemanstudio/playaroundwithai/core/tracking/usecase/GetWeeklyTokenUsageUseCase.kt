package se.onemanstudio.playaroundwithai.core.tracking.usecase

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.tracking.model.DailyTokenUsage
import se.onemanstudio.playaroundwithai.core.tracking.repository.TokenUsageQuery
import javax.inject.Inject

class GetWeeklyTokenUsageUseCase @Inject constructor(
    private val tokenUsageQuery: TokenUsageQuery,
) {
    operator fun invoke(): Flow<List<DailyTokenUsage>> = tokenUsageQuery.getWeeklyUsage()
}
