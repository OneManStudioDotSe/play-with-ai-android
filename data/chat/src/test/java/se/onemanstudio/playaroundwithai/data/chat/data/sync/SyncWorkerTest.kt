package se.onemanstudio.playaroundwithai.data.chat.data.sync

import android.content.Context
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.data.chat.data.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.data.chat.data.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.data.chat.data.remote.FirestoreDataSource
import se.onemanstudio.playaroundwithai.data.chat.domain.model.SyncStatus

class SyncWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var promptsDao: PromptsHistoryDao
    private lateinit var firestoreDataSource: FirestoreDataSource
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        promptsDao = mockk(relaxed = true)
        firestoreDataSource = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        every { authRepository.isUserSignedIn() } returns true
    }

    private fun createWorker(): SyncWorker {
        val worker = spyk(
            SyncWorker(context, workerParams, promptsDao, firestoreDataSource, authRepository)
        )
        val mockForegroundInfo = mockk<ForegroundInfo>()
        every { worker["createForegroundInfo"]() } returns mockForegroundInfo
        every { worker["showFailureNotification"](any<Int>()) } returns Unit
        coEvery { worker.setForeground(any<ForegroundInfo>()) } returns Unit
        return worker
    }

    @Test
    fun `doWork returns failure when user is not authenticated`() = runTest {
        every { authRepository.isUserSignedIn() } returns false

        val result = createWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        coVerify(exactly = 0) { promptsDao.getPromptsBySyncStatus(any()) }
    }

    @Test
    fun `doWork returns success when no pending prompts exist`() = runTest {
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns emptyList()

        val result = createWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `doWork creates new Firestore document for prompt without firestoreDocId`() = runTest {
        val entity = PromptEntity(id = 1, text = "Hello", timestamp = 1000L, syncStatus = SyncStatus.Pending)
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.savePrompt("Hello", 1000L) } returns Result.success("doc123")
        coEvery { promptsDao.markSyncedIfTextMatches(1, "Hello", SyncStatus.Synced.name) } returns 1

        val result = createWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify { promptsDao.updateFirestoreDocId(1, "doc123") }
        coVerify { promptsDao.markSyncedIfTextMatches(1, "Hello", SyncStatus.Synced.name) }
    }

    @Test
    fun `doWork updates existing Firestore document for prompt with firestoreDocId`() = runTest {
        val entity = PromptEntity(
            id = 2, text = "Updated text", timestamp = 2000L, syncStatus = SyncStatus.Pending, firestoreDocId = "existingDoc"
        )
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.updatePrompt("existingDoc", "Updated text") } returns Result.success(Unit)
        coEvery { promptsDao.markSyncedIfTextMatches(2, "Updated text", SyncStatus.Synced.name) } returns 1

        val result = createWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { firestoreDataSource.savePrompt(any(), any()) }
        coVerify { firestoreDataSource.updatePrompt("existingDoc", "Updated text") }
    }

    @Test
    fun `doWork retries when sync fails and attempts remain`() = runTest {
        val entity = PromptEntity(id = 1, text = "Fail me", timestamp = 1000L, syncStatus = SyncStatus.Pending)
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.savePrompt(any(), any()) } returns Result.failure(Exception("Firestore error"))
        every { workerParams.runAttemptCount } returns 0

        val result = createWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun `doWork marks prompts as Failed after all retries exhausted`() = runTest {
        val entity = PromptEntity(id = 1, text = "Fail me", timestamp = 1000L, syncStatus = SyncStatus.Pending)
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.savePrompt(any(), any()) } returns Result.failure(Exception("Firestore error"))
        every { workerParams.runAttemptCount } returns 2

        val result = createWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        coVerify { promptsDao.updateSyncStatus(1, SyncStatus.Failed.name) }
    }
}
