package se.onemanstudio.playaroundwithai.data.chat.data.repository

import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.data.chat.data.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.data.chat.data.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.data.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.data.chat.domain.model.SyncStatus
import java.time.Instant

class PromptRepositoryImplTest {

    private lateinit var dao: PromptsHistoryDao
    private lateinit var workManager: WorkManager
    private lateinit var authRepository: AuthRepository
    private lateinit var repository: PromptRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        every { authRepository.isUserSignedIn() } returns true
        repository = PromptRepositoryImpl(dao, workManager, authRepository)
    }

    @Test
    fun `savePrompt sets syncStatus to Pending before saving to DAO`() = runTest {
        val prompt = Prompt(
            id = 0L,
            text = "Test prompt",
            timestamp = Instant.ofEpochMilli(1_700_000_000_000L),
            syncStatus = SyncStatus.Synced
        )
        val entitySlot = slot<PromptEntity>()
        coEvery { dao.savePrompt(capture(entitySlot)) } returns 1L

        repository.savePrompt(prompt)

        assertThat(entitySlot.captured.syncStatus).isEqualTo(SyncStatus.Pending)
        assertThat(entitySlot.captured.text).isEqualTo("Test prompt")
    }

    @Test
    fun `savePrompt schedules sync when user is authenticated`() = runTest {
        val prompt = Prompt(id = 0L, text = "Sync me", timestamp = Instant.now(), syncStatus = SyncStatus.Pending)
        coEvery { dao.savePrompt(any()) } returns 1L

        repository.savePrompt(prompt)

        verify { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `savePrompt does not schedule sync when user is not authenticated`() = runTest {
        every { authRepository.isUserSignedIn() } returns false
        val prompt = Prompt(id = 0L, text = "No sync", timestamp = Instant.now(), syncStatus = SyncStatus.Pending)
        coEvery { dao.savePrompt(any()) } returns 1L

        repository.savePrompt(prompt)

        coVerify { dao.savePrompt(any()) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `getPromptHistory maps entities from DAO to domain models`() = runTest {
        val entities = listOf(
            PromptEntity(id = 1, text = "First", timestamp = 1_700_000_000_000L, syncStatus = SyncStatus.Synced),
            PromptEntity(id = 2, text = "Second", timestamp = 1_700_000_060_000L, syncStatus = SyncStatus.Pending)
        )
        every { dao.getPromptHistory() } returns flowOf(entities)

        val result = repository.getPromptHistory().first()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(1L)
        assertThat(result[0].text).isEqualTo("First")
        assertThat(result[0].syncStatus).isEqualTo(SyncStatus.Synced)
        assertThat(result[1].id).isEqualTo(2L)
        assertThat(result[1].syncStatus).isEqualTo(SyncStatus.Pending)
    }

    @Test
    fun `getPromptHistory returns empty list when DAO has no data`() = runTest {
        every { dao.getPromptHistory() } returns flowOf(emptyList())

        val result = repository.getPromptHistory().first()

        assertThat(result).isEmpty()
    }

    @Test
    fun `getFailedSyncCount delegates to DAO with Failed status`() = runTest {
        every { dao.getCountBySyncStatus(SyncStatus.Failed.name) } returns flowOf(3)

        val count = repository.getFailedSyncCount().first()

        assertThat(count).isEqualTo(3)
    }

    @Test
    fun `savePrompt returns inserted row id`() = runTest {
        val prompt = Prompt(id = 0L, text = "Test", timestamp = Instant.now(), syncStatus = SyncStatus.Pending)
        coEvery { dao.savePrompt(any()) } returns 42L

        val result = repository.savePrompt(prompt)

        assertThat(result).isEqualTo(42L)
    }

    @Test
    fun `updatePromptText updates text resets sync status and schedules sync`() = runTest {
        coEvery { dao.updatePromptText(5L, "Updated text") } returns Unit
        coEvery { dao.updateSyncStatus(5L, SyncStatus.Pending.name) } returns Unit

        repository.updatePromptText(5L, "Updated text")

        coVerify { dao.updatePromptText(5L, "Updated text") }
        coVerify { dao.updateSyncStatus(5L, SyncStatus.Pending.name) }
        verify { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `updatePromptText does not schedule sync when user is not authenticated`() = runTest {
        every { authRepository.isUserSignedIn() } returns false
        coEvery { dao.updatePromptText(5L, "Updated text") } returns Unit
        coEvery { dao.updateSyncStatus(5L, SyncStatus.Pending.name) } returns Unit

        repository.updatePromptText(5L, "Updated text")

        coVerify { dao.updatePromptText(5L, "Updated text") }
        coVerify { dao.updateSyncStatus(5L, SyncStatus.Pending.name) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `retryPendingSyncs resets Failed to Pending and schedules sync`() = runTest {
        coEvery { dao.updateAllSyncStatuses(SyncStatus.Failed.name, SyncStatus.Pending.name) } returns Unit

        repository.retryPendingSyncs()

        coVerify { dao.updateAllSyncStatuses(SyncStatus.Failed.name, SyncStatus.Pending.name) }
        verify { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `retryPendingSyncs does not schedule sync when user is not authenticated`() = runTest {
        every { authRepository.isUserSignedIn() } returns false
        coEvery { dao.updateAllSyncStatuses(any(), any()) } returns Unit

        repository.retryPendingSyncs()

        coVerify { dao.updateAllSyncStatuses(SyncStatus.Failed.name, SyncStatus.Pending.name) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }
}
