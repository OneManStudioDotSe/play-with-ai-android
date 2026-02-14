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
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.services.SyncWorker
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import se.onemanstudio.playaroundwithai.core.data.feature.chat.mapper.toDomain as toPromptDomain
import se.onemanstudio.playaroundwithai.core.data.feature.chat.mapper.toEntity as toPromptEntity

private const val SYNC_WORK_NAME = "sync_prompts_work"
private const val LOG_PREVIEW_LENGTH = 50

@Singleton
class PromptRepositoryImpl @Inject constructor(
    private val promptsHistoryDao: PromptsHistoryDao,
    private val workManager: WorkManager,
    private val authRepository: AuthRepository
) : PromptRepository {

    override suspend fun savePrompt(prompt: Prompt) {
        Timber.d("PromptRepo - Saving prompt to local DB, with text '${prompt.text.take(LOG_PREVIEW_LENGTH)}...', syncStatus: Pending")
        val promptWithPendingStatus = prompt.copy(syncStatus = SyncStatus.Pending)
        promptsHistoryDao.savePrompt(promptWithPendingStatus.toPromptEntity())

        if (authRepository.isUserSignedIn()) {
            Timber.d("PromptRepo - Prompt saved to Room. Scheduling background sync...")
            scheduleSync()
        } else {
            Timber.w("PromptRepo - Prompt saved to Room. Skipping sync — user is not authenticated")
        }
    }

    override fun getPromptHistory(): Flow<List<Prompt>> =
        promptsHistoryDao.getPromptHistory().map { list ->
            Timber.v("PromptRepo - Prompt history updated. We now have ${list.size} entries at the local DB")
            list.map { it.toPromptDomain() }
        }

    override fun isSyncing(): Flow<Boolean> {
        return workManager
            .getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { workInfos ->
                val syncing = workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
                Timber.v("PromptRepo - Sync status check -> isSyncing:$syncing")
                syncing
            }
    }

    override fun getFailedSyncCount(): Flow<Int> {
        return promptsHistoryDao.getCountBySyncStatus(SyncStatus.Failed.name)
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SYNC_WORK_NAME)
            .build()

        Timber.d("PromptRepo - Enqueuing SyncWorker...")
        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
