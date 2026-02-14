package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.database.AppDatabase
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository.AuthRepository // Added import
import java.util.Date

@RunWith(AndroidJUnit4::class)
class PromptRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var promptsHistoryDao: PromptsHistoryDao
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true) // Added mock
    private lateinit var repository: PromptRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        promptsHistoryDao = database.historyDao()
        repository = PromptRepositoryImpl(promptsHistoryDao, workManager, authRepository) // Updated constructor
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getPromptHistory_whenDataExists_returnsMappedDomainModels() = runBlocking {
        // Given
        val promptEntity = PromptEntity(id = 1, text = "Test prompt", timestamp = 12345L, syncStatus = SyncStatus.Synced)
        promptsHistoryDao.savePrompt(promptEntity)

        // When
        val result = repository.getPromptHistory().first()

        // Then
        assertThat(result).hasSize(1)
        val promptDomainModel = result.first()
        assertThat(promptDomainModel.id).isEqualTo(1)
        assertThat(promptDomainModel.text).isEqualTo("Test prompt")
        assertThat(promptDomainModel.timestamp).isEqualTo(Date(12345L))
    }

    @Test
    fun getPromptHistory_whenDbIsEmpty_returnsEmptyList() = runBlocking {
        // When
        val result = repository.getPromptHistory().first()

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun getFailedSyncCount_returnsCorrectCount() = runBlocking {
        // Given
        promptsHistoryDao.savePrompt(PromptEntity(id = 1, text = "Failed 1", timestamp = 1L, syncStatus = SyncStatus.Failed))
        promptsHistoryDao.savePrompt(PromptEntity(id = 2, text = "Failed 2", timestamp = 2L, syncStatus = SyncStatus.Failed))
        promptsHistoryDao.savePrompt(PromptEntity(id = 3, text = "Synced", timestamp = 3L, syncStatus = SyncStatus.Synced))

        // When
        val result = repository.getFailedSyncCount().first()

        // Then
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun getFailedSyncCount_whenNoneFailed_returnsZero() = runBlocking {
        // Given
        promptsHistoryDao.savePrompt(PromptEntity(id = 1, text = "Synced", timestamp = 1L, syncStatus = SyncStatus.Synced))

        // When
        val result = repository.getFailedSyncCount().first()

        // Then
        assertThat(result).isEqualTo(0)
    }
}