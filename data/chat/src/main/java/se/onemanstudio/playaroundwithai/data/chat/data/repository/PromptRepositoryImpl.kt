package se.onemanstudio.playaroundwithai.data.chat.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.core.config.settings.AppSettingsHolder
import se.onemanstudio.playaroundwithai.core.database.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.database.entity.SyncStatus
import se.onemanstudio.playaroundwithai.data.chat.data.sync.SyncWorker
import se.onemanstudio.playaroundwithai.data.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.PromptRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import se.onemanstudio.playaroundwithai.data.chat.data.mapper.toDomain as toPromptDomain
import se.onemanstudio.playaroundwithai.data.chat.data.mapper.toEntity as toPromptEntity

private const val SYNC_WORK_NAME = "sync_prompts_work"
private const val BACKOFF_DELAY_SECONDS = 30L

@Singleton
class PromptRepositoryImpl @Inject constructor(
    private val promptsHistoryDao: PromptsHistoryDao,
    private val workManager: WorkManager,
    private val authRepository: AuthRepository,
    private val appSettingsHolder: AppSettingsHolder,
) : PromptRepository {

    override suspend fun savePrompt(prompt: Prompt): Long {
        val promptWithPendingStatus = prompt.copy(syncStatus = SyncStatus.Pending)
        val insertedId = promptsHistoryDao.savePrompt(promptWithPendingStatus.toPromptEntity())

        if (authRepository.isUserSignedIn() && appSettingsHolder.firebaseSyncEnabled.value) {
            scheduleSync()
        } else {
            Timber.w("PromptRepo - Skipping sync — user is not authenticated or sync is disabled")
        }

        return insertedId
    }

    override suspend fun updatePromptText(id: Long, text: String) {
        promptsHistoryDao.updatePromptText(id, text)
        promptsHistoryDao.updateSyncStatus(id, SyncStatus.Pending)

        if (authRepository.isUserSignedIn() && appSettingsHolder.firebaseSyncEnabled.value) {
            scheduleSync()
        } else {
            Timber.w("PromptRepo - Text updated. Skipping sync — user is not authenticated or sync is disabled")
        }
    }

    override suspend fun retryPendingSyncs() {
        promptsHistoryDao.updateAllSyncStatuses(SyncStatus.Failed, SyncStatus.Pending)

        if (authRepository.isUserSignedIn() && appSettingsHolder.firebaseSyncEnabled.value) {
            scheduleSync()
        } else {
            Timber.w("PromptRepo - Skipping sync retry — user is not authenticated or sync is disabled")
        }
    }

    override fun getPromptHistory(): Flow<List<Prompt>> =
        promptsHistoryDao.getPromptHistory().map { list ->
            list.map { it.toPromptDomain() }
        }

    override fun isSyncing(): Flow<Boolean> {
        return workManager
            .getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { workInfos ->
                val syncing =
                    workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
                syncing
            }
    }

    override fun getFailedSyncCount(): Flow<Int> {
        return promptsHistoryDao.getCountBySyncStatus(SyncStatus.Failed)
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

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            syncRequest
        )
    }
}
