package se.onemanstudio.playaroundwithai.data.chat.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.data.chat.data.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.data.chat.data.sync.SyncWorker
import se.onemanstudio.playaroundwithai.data.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.data.chat.domain.model.SyncStatus
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.PromptRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import se.onemanstudio.playaroundwithai.data.chat.data.mapper.toDomain as toPromptDomain
import se.onemanstudio.playaroundwithai.data.chat.data.mapper.toEntity as toPromptEntity

private const val SYNC_WORK_NAME = "sync_prompts_work"
private const val LOG_PREVIEW_LENGTH = 50
private const val BACKOFF_DELAY_SECONDS = 30L

@Singleton
class PromptRepositoryImpl @Inject constructor(
    private val promptsHistoryDao: PromptsHistoryDao,
    private val workManager: WorkManager,
    private val authRepository: AuthRepository
) : PromptRepository {

    override suspend fun savePrompt(prompt: Prompt): Long {
        Timber.d("PromptRepo - Saving prompt to local DB, with text '${prompt.text.take(LOG_PREVIEW_LENGTH)}...', syncStatus: Pending")
        val promptWithPendingStatus = prompt.copy(syncStatus = SyncStatus.Pending)
        val insertedId = promptsHistoryDao.savePrompt(promptWithPendingStatus.toPromptEntity())
        Timber.d("PromptRepo - Prompt saved to Room (id=$insertedId)")

        if (authRepository.isUserSignedIn()) {
            Timber.d("PromptRepo - Scheduling immediate sync for the question...")
            scheduleSync()
        } else {
            Timber.w("PromptRepo - Skipping sync — user is not authenticated")
        }

        return insertedId
    }

    override suspend fun updatePromptText(id: Long, text: String) {
        Timber.d("PromptRepo - Updating prompt text for id=$id, text='${text.take(LOG_PREVIEW_LENGTH)}...'")
        promptsHistoryDao.updatePromptText(id.toInt(), text)
        promptsHistoryDao.updateSyncStatus(id.toInt(), SyncStatus.Pending.name)

        if (authRepository.isUserSignedIn()) {
            Timber.d("PromptRepo - Text updated. Scheduling sync for the complete Q&A...")
            scheduleSync()
        } else {
            Timber.w("PromptRepo - Text updated. Skipping sync — user is not authenticated")
        }
    }

    override suspend fun retryPendingSyncs() {
        Timber.d("PromptRepo - Retrying failed syncs: resetting Failed → Pending")
        promptsHistoryDao.updateAllSyncStatuses(SyncStatus.Failed.name, SyncStatus.Pending.name)

        if (authRepository.isUserSignedIn()) {
            Timber.d("PromptRepo - Re-enqueuing SyncWorker for failed prompts")
            scheduleSync()
        } else {
            Timber.w("PromptRepo - Skipping sync retry — user is not authenticated")
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
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
            .addTag(SYNC_WORK_NAME)
            .build()

        Timber.d("PromptRepo - Enqueuing SyncWorker...")
        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            syncRequest
        )
    }
}
