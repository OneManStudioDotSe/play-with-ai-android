package se.onemanstudio.playaroundwithai.data.dream.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import javax.inject.Inject

class GetDreamHistoryUseCase @Inject constructor(
    private val repository: DreamRepository
) {
    operator fun invoke(): Flow<List<Dream>> = repository.getDreamHistory()
        .map { dreams ->
            dreams.sortedByDescending { it.timestamp }
        }
}
