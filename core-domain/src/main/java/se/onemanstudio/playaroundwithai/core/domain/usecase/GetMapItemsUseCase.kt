package se.onemanstudio.playaroundwithai.core.domain.usecase

import se.onemanstudio.playaroundwithai.core.domain.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.repository.MapDomainRepository
import javax.inject.Inject

class GetMapItemsUseCase @Inject constructor(
    private val repository: MapDomainRepository
) {
    suspend operator fun invoke(count: Int): List<MapItem> {
        return repository.getMapItems(count)
    }
}
