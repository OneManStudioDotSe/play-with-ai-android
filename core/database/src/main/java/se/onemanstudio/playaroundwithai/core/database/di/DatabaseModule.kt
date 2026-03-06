package se.onemanstudio.playaroundwithai.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.core.database.AppDatabase
import se.onemanstudio.playaroundwithai.core.database.dao.DreamsDao
import se.onemanstudio.playaroundwithai.core.database.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.database.dao.TokenUsageDao
import javax.inject.Singleton

private const val DATABASE_NAME = "play_with_ai_db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
            )
            .build()
    }

    @Provides
    fun providePromptsHistoryDao(appDatabase: AppDatabase): PromptsHistoryDao =
        appDatabase.historyDao()

    @Provides
    fun provideTokenUsageDao(appDatabase: AppDatabase): TokenUsageDao =
        appDatabase.tokenUsageDao()

    @Provides
    fun provideDreamsDao(appDatabase: AppDatabase): DreamsDao =
        appDatabase.dreamsDao()
}
