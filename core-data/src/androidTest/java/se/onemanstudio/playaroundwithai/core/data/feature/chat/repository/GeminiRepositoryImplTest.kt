package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.GeminiApiService

@RunWith(AndroidJUnit4::class)
class GeminiRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var promptsHistoryDao: PromptsHistoryDao
    private val geminiApiService: GeminiApiService = mockk(relaxed = true) // Mocked, as we're not testing the network

    private lateinit var repository: GeminiRepositoryImpl

    @Before
    fun setup() {
        // Create a real in-memory database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        // Get a real DAO instance from the database
        promptsHistoryDao = database.promptDao()

        // Create the repository with a real DAO and a mock API service
        repository = GeminiRepositoryImpl(geminiApiService, promptsHistoryDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getPromptHistory_whenDataExists_returnsMappedDomainModels() = runBlocking {
        // Given: We insert an Entity directly into the database using the DAO
        val promptEntity = PromptEntity(id = 1, text = "Test prompt", timestamp = 12345L)
        promptsHistoryDao.insertPrompt(promptEntity)

        // When: We call the repository method
        val promptHistoryFlow = repository.getPromptHistory()
        val result = promptHistoryFlow.first()

        // Then: The repository should have returned a correctly mapped domain model
        assertThat(result).hasSize(1)
        val promptDomainModel = result.first()
        assertThat(promptDomainModel.id).isEqualTo(1)
        assertThat(promptDomainModel.text).isEqualTo("Test prompt")
        assertThat(promptDomainModel.timestamp).isEqualTo(12345L)
    }

    @Test
    fun getPromptHistory_whenDbIsEmpty_returnsEmptyList() = runBlocking {
        // When
        val result = repository.getPromptHistory().first()

        // Then
        assertThat(result).isEmpty()
    }
}
