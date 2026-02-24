package se.onemanstudio.playaroundwithai.data.dream.domain.usecase

import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import javax.inject.Inject

class SaveDreamUseCase @Inject constructor(
    private val repository: DreamRepository
) {
    suspend operator fun invoke(dream: Dream): Long {
        require(dream.description.isNotBlank()) { "Dream description must not be blank" }

        return repository.saveDream(dream)
    }
}
