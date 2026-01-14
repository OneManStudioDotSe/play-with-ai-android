package se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.feature.map.repository.MapRepository
import javax.inject.Inject

class GetMapItemsUseCase @Inject constructor(
    private val repository: MapRepository
) {
    suspend operator fun invoke(count: Int): List<MapItem> {
        return repository.getMapItems(count)
    }
}