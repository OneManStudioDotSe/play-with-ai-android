package se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt

interface PromptRepository {
    suspend fun savePrompt(prompt: Prompt)

    fun getPromptHistory(): Flow<List<Prompt>>

    fun isSyncing(): Flow<Boolean>

    fun getFailedSyncCount(): Flow<Int>
}