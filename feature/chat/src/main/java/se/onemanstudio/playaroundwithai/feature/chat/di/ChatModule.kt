package se.onemanstudio.playaroundwithai.feature.chat.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.feature.chat.data.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.feature.chat.data.local.database.AppDatabase
import se.onemanstudio.playaroundwithai.feature.chat.data.repository.ChatGeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.feature.chat.data.repository.PromptRepositoryImpl
import se.onemanstudio.playaroundwithai.feature.chat.domain.repository.ChatGeminiRepository
import se.onemanstudio.playaroundwithai.feature.chat.domain.repository.PromptRepository
import javax.inject.Singleton

private const val DATABASE = "play_with_ai_db"

@Module
@InstallIn(SingletonComponent::class)
object ChatDataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides
    fun providePromptsHistoryDao(appDatabase: AppDatabase): PromptsHistoryDao {
        return appDatabase.historyDao()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatBindingsModule {

    @Binds
    @Singleton
    abstract fun bindPromptRepository(impl: PromptRepositoryImpl): PromptRepository

    @Binds
    @Singleton
    abstract fun bindChatGeminiRepository(impl: ChatGeminiRepositoryImpl): ChatGeminiRepository
}
