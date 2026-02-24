package se.onemanstudio.playaroundwithai.data.dream.domain.usecase

import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import javax.inject.Inject

class DeleteDreamUseCase @Inject constructor(
    private val repository: DreamRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteDream(id)
}
