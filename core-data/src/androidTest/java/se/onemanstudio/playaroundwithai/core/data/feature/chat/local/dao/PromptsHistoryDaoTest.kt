package se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.database.AppDatabase
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PromptsHistoryDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: PromptsHistoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        dao = db.historyDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetHistory_returnsCorrectData() = runBlocking {
        // Given
        val prompt = PromptEntity(text = "Tell me a fun fact", timestamp = System.currentTimeMillis())
        dao.savePrompt(prompt)

        // When
        val history = dao.getPromptHistory().first()

        // Then
        assertThat(history).hasSize(1)
        assertThat(history.first().text).isEqualTo("Tell me a fun fact")
    }

    @Test
    @Throws(Exception::class)
    fun getHistory_whenEmpty_returnsEmptyList() = runBlocking {
        // When
        val history = dao.getPromptHistory().first()

        // Then
        assertThat(history).isEmpty()
    }
}
