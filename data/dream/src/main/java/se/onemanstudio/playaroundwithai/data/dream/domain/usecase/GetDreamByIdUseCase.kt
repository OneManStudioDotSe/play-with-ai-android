package se.onemanstudio.playaroundwithai.data.dream.domain.usecase

import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import javax.inject.Inject

class GetDreamByIdUseCase @Inject constructor(
    private val repository: DreamRepository
) {
    suspend operator fun invoke(id: Long): Dream? = repository.getDreamById(id)
}
