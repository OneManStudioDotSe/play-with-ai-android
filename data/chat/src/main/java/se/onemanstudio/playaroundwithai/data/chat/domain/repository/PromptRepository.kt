package se.onemanstudio.playaroundwithai.data.chat.domain.repository

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.data.chat.domain.model.Prompt

interface PromptRepository {
    suspend fun savePrompt(prompt: Prompt): Long

    suspend fun updatePromptText(id: Long, text: String)

    suspend fun retryPendingSyncs()

    fun getPromptHistory(): Flow<List<Prompt>>

    fun isSyncing(): Flow<Boolean>

    fun getFailedSyncCount(): Flow<Int>
}
