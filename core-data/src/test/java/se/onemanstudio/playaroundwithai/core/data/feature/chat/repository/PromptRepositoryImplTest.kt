package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

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
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import java.util.Date

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
        // GIVEN: A prompt with Synced status
        val prompt = Prompt(
            id = 0L,
            text = "Test prompt",
            timestamp = Date(1_700_000_000_000L),
            syncStatus = SyncStatus.Synced
        )
        val entitySlot = slot<PromptEntity>()
        coEvery { dao.savePrompt(capture(entitySlot)) } returns 1L

        // WHEN
        repository.savePrompt(prompt)

        // THEN: The entity saved to DAO should have Pending status, regardless of input
        assertThat(entitySlot.captured.syncStatus).isEqualTo(SyncStatus.Pending)
        assertThat(entitySlot.captured.text).isEqualTo("Test prompt")
    }

    @Test
    fun `savePrompt schedules sync when user is authenticated`() = runTest {
        // GIVEN
        val prompt = Prompt(id = 0L, text = "Sync me", timestamp = Date(), syncStatus = SyncStatus.Pending)
        coEvery { dao.savePrompt(any()) } returns 1L

        // WHEN
        repository.savePrompt(prompt)

        // THEN: Sync is scheduled immediately so the question reaches Firestore
        verify { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `savePrompt does not schedule sync when user is not authenticated`() = runTest {
        // GIVEN
        every { authRepository.isUserSignedIn() } returns false
        val prompt = Prompt(id = 0L, text = "No sync", timestamp = Date(), syncStatus = SyncStatus.Pending)
        coEvery { dao.savePrompt(any()) } returns 1L

        // WHEN
        repository.savePrompt(prompt)

        // THEN: Prompt is saved locally but sync is NOT scheduled
        coVerify { dao.savePrompt(any()) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `getPromptHistory maps entities from DAO to domain models`() = runTest {
        // GIVEN: DAO returns a flow of entities
        val entities = listOf(
            PromptEntity(id = 1, text = "First", timestamp = 1_700_000_000_000L, syncStatus = SyncStatus.Synced),
            PromptEntity(id = 2, text = "Second", timestamp = 1_700_000_060_000L, syncStatus = SyncStatus.Pending)
        )
        every { dao.getPromptHistory() } returns flowOf(entities)

        // WHEN
        val result = repository.getPromptHistory().first()

        // THEN: Entities are mapped to domain models
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(1L)
        assertThat(result[0].text).isEqualTo("First")
        assertThat(result[0].syncStatus).isEqualTo(SyncStatus.Synced)
        assertThat(result[1].id).isEqualTo(2L)
        assertThat(result[1].syncStatus).isEqualTo(SyncStatus.Pending)
    }

    @Test
    fun `getPromptHistory returns empty list when DAO has no data`() = runTest {
        // GIVEN: DAO returns an empty flow
        every { dao.getPromptHistory() } returns flowOf(emptyList())

        // WHEN
        val result = repository.getPromptHistory().first()

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun `getFailedSyncCount delegates to DAO with Failed status`() = runTest {
        // GIVEN: DAO returns a count of failed syncs
        every { dao.getCountBySyncStatus(SyncStatus.Failed.name) } returns flowOf(3)

        // WHEN
        val count = repository.getFailedSyncCount().first()

        // THEN
        assertThat(count).isEqualTo(3)
    }

    @Test
    fun `savePrompt returns inserted row id`() = runTest {
        // GIVEN
        val prompt = Prompt(id = 0L, text = "Test", timestamp = Date(), syncStatus = SyncStatus.Pending)
        coEvery { dao.savePrompt(any()) } returns 42L

        // WHEN
        val result = repository.savePrompt(prompt)

        // THEN
        assertThat(result).isEqualTo(42L)
    }

    @Test
    fun `updatePromptText updates text resets sync status and schedules sync`() = runTest {
        // GIVEN
        coEvery { dao.updatePromptText(5, "Updated text") } returns Unit
        coEvery { dao.updateSyncStatus(5, SyncStatus.Pending.name) } returns Unit

        // WHEN
        repository.updatePromptText(5L, "Updated text")

        // THEN
        coVerify { dao.updatePromptText(5, "Updated text") }
        coVerify { dao.updateSyncStatus(5, SyncStatus.Pending.name) }
        verify { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `updatePromptText does not schedule sync when user is not authenticated`() = runTest {
        // GIVEN
        every { authRepository.isUserSignedIn() } returns false
        coEvery { dao.updatePromptText(5, "Updated text") } returns Unit
        coEvery { dao.updateSyncStatus(5, SyncStatus.Pending.name) } returns Unit

        // WHEN
        repository.updatePromptText(5L, "Updated text")

        // THEN: Text and status are updated but sync is NOT scheduled
        coVerify { dao.updatePromptText(5, "Updated text") }
        coVerify { dao.updateSyncStatus(5, SyncStatus.Pending.name) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `retryPendingSyncs resets Failed to Pending and schedules sync`() = runTest {
        // GIVEN
        coEvery { dao.updateAllSyncStatuses(SyncStatus.Failed.name, SyncStatus.Pending.name) } returns Unit

        // WHEN
        repository.retryPendingSyncs()

        // THEN
        coVerify { dao.updateAllSyncStatuses(SyncStatus.Failed.name, SyncStatus.Pending.name) }
        verify { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `retryPendingSyncs does not schedule sync when user is not authenticated`() = runTest {
        // GIVEN
        every { authRepository.isUserSignedIn() } returns false
        coEvery { dao.updateAllSyncStatuses(any(), any()) } returns Unit

        // WHEN
        repository.retryPendingSyncs()

        // THEN: Status is updated but sync is NOT scheduled
        coVerify { dao.updateAllSyncStatuses(SyncStatus.Failed.name, SyncStatus.Pending.name) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }
}
