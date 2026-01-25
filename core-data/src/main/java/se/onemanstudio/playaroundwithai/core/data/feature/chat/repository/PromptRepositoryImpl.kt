package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.firebase.SyncWorker
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import se.onemanstudio.playaroundwithai.core.data.feature.chat.mapper.toDomain as toPromptDomain
import se.onemanstudio.playaroundwithai.core.data.feature.chat.mapper.toEntity as toPromptEntity
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_WORK_NAME = "sync_prompts_work"

@Singleton
class PromptRepositoryImpl @Inject constructor(
    private val promptsHistoryDao: PromptsHistoryDao,
    private val workManager: WorkManager
) : PromptRepository {

    override suspend fun savePrompt(prompt: Prompt) {
        val promptWithPendingStatus = prompt.copy(syncStatus = SyncStatus.Pending)
        promptsHistoryDao.savePrompt(promptWithPendingStatus.toPromptEntity())
        scheduleSync()
    }

    override fun getPromptHistory(): Flow<List<Prompt>> = 
        promptsHistoryDao.getPromptHistory().map { list -> 
            list.map { it.toPromptDomain() }
        }

    override fun isSyncing(): Flow<Boolean> {
        return workManager
            .getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SYNC_WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}