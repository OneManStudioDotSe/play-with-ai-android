package se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.services

import android.content.Context
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.FirestoreDataSource
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus

class SyncWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var promptsDao: PromptsHistoryDao
    private lateinit var firestoreDataSource: FirestoreDataSource
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var worker: SyncWorker

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        promptsDao = mockk(relaxed = true)
        firestoreDataSource = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)

        every { workerParams.runAttemptCount } returns 0

        worker = spyk(
            SyncWorker(context, workerParams, promptsDao, firestoreDataSource, firebaseAuth),
            recordPrivateCalls = true
        )
        every { worker["createForegroundInfo"]() } returns mockk<ForegroundInfo>()
        every { worker["showFailureNotification"](any<Int>()) } returns Unit
        coEvery { worker.setForeground(any()) } returns Unit
    }

    @Test
    fun `doWork when user is not authenticated returns failure`() = runTest {
        // GIVEN: No authenticated user
        every { firebaseAuth.currentUser } returns null

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(Result.failure())
    }

    @Test
    fun `doWork when no pending prompts returns success`() = runTest {
        // GIVEN: Authenticated user but no pending prompts
        every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns emptyList()

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(Result.success())
    }

    @Test
    fun `doWork creates new Firestore document for prompt without docId`() = runTest {
        // GIVEN: A pending prompt without a Firestore document ID
        val entity = PromptEntity(id = 1, text = "Hello", timestamp = 1000L, syncStatus = SyncStatus.Pending, firestoreDocId = null)
        every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.savePrompt("Hello", 1000L) } returns kotlin.Result.success("doc-123")
        coEvery { promptsDao.markSyncedIfTextMatches(1, "Hello", SyncStatus.Synced.name) } returns 1

        // WHEN
        val result = worker.doWork()

        // THEN: Creates new doc and stores the returned docId
        coVerify { firestoreDataSource.savePrompt("Hello", 1000L) }
        coVerify { promptsDao.updateFirestoreDocId(1, "doc-123") }
        coVerify { promptsDao.markSyncedIfTextMatches(1, "Hello", SyncStatus.Synced.name) }
        assertThat(result).isEqualTo(Result.success())
    }

    @Test
    fun `doWork updates existing Firestore document for prompt with docId`() = runTest {
        // GIVEN: A pending prompt that already has a Firestore document ID
        val entity = PromptEntity(id = 2, text = "Updated", timestamp = 2000L, syncStatus = SyncStatus.Pending, firestoreDocId = "existing-doc")
        every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.updatePrompt("existing-doc", "Updated") } returns kotlin.Result.success(Unit)
        coEvery { promptsDao.markSyncedIfTextMatches(2, "Updated", SyncStatus.Synced.name) } returns 1

        // WHEN
        val result = worker.doWork()

        // THEN: Updates existing doc instead of creating new one
        coVerify { firestoreDataSource.updatePrompt("existing-doc", "Updated") }
        coVerify(exactly = 0) { firestoreDataSource.savePrompt(any(), any()) }
        assertThat(result).isEqualTo(Result.success())
    }

    @Test
    fun `doWork retries when sync fails and attempts not exhausted`() = runTest {
        // GIVEN: A prompt that fails to sync, first attempt
        val entity = PromptEntity(id = 1, text = "Fail", timestamp = 1000L, syncStatus = SyncStatus.Pending, firestoreDocId = null)
        every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()
        every { workerParams.runAttemptCount } returns 0
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.savePrompt(any(), any()) } returns kotlin.Result.failure(Exception("Network error"))

        // WHEN
        val result = worker.doWork()

        // THEN: Returns retry since attempts remain
        assertThat(result).isEqualTo(Result.retry())
    }

    @Test
    fun `doWork marks prompts as Failed when all retries exhausted`() = runTest {
        // GIVEN: A prompt that fails to sync, final attempt (attempt index 2 = 3rd attempt)
        val entity = PromptEntity(id = 1, text = "Fail", timestamp = 1000L, syncStatus = SyncStatus.Pending, firestoreDocId = null)
        every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()
        every { workerParams.runAttemptCount } returns 2
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.savePrompt(any(), any()) } returns kotlin.Result.failure(Exception("Network error"))

        // WHEN
        val result = worker.doWork()

        // THEN: Marks prompts as Failed and returns failure
        coVerify { promptsDao.updateSyncStatus(1, SyncStatus.Failed.name) }
        assertThat(result).isEqualTo(Result.failure())
    }

    @Test
    fun `doWork does not mark as synced when text changed during sync (race condition)`() = runTest {
        // GIVEN: A prompt that syncs successfully but text changed during sync
        val entity = PromptEntity(id = 1, text = "Original", timestamp = 1000L, syncStatus = SyncStatus.Pending, firestoreDocId = null)
        every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()
        coEvery { promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name) } returns listOf(entity)
        coEvery { firestoreDataSource.savePrompt("Original", 1000L) } returns kotlin.Result.success("doc-1")
        // markSyncedIfTextMatches returns 0 because text was modified while sync was in progress
        coEvery { promptsDao.markSyncedIfTextMatches(1, "Original", SyncStatus.Synced.name) } returns 0

        // WHEN
        val result = worker.doWork()

        // THEN: Sync succeeds overall but prompt stays Pending (0 rows updated)
        coVerify { promptsDao.markSyncedIfTextMatches(1, "Original", SyncStatus.Synced.name) }
        assertThat(result).isEqualTo(Result.success())
    }
}
