package se.onemanstudio.playaroundwithai.data.dream.domain.repository

import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamInterpretation

interface DreamGeminiRepository {
    suspend fun interpretDream(description: String): Result<DreamInterpretation>
}
