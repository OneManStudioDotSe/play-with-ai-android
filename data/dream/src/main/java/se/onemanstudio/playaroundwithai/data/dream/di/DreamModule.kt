package se.onemanstudio.playaroundwithai.data.dream.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.data.dream.data.local.dao.DreamsDao
import se.onemanstudio.playaroundwithai.data.dream.data.local.database.DreamDatabase
import se.onemanstudio.playaroundwithai.data.dream.data.repository.DreamGeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.data.dream.data.repository.DreamRepositoryImpl
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamGeminiRepository
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import javax.inject.Singleton

private const val DATABASE = "dream_db"

@Module
@InstallIn(SingletonComponent::class)
object DreamDataModule {

    @Provides
    @Singleton
    fun provideDreamDatabase(@ApplicationContext context: Context): DreamDatabase {
        return Room.databaseBuilder(context, DreamDatabase::class.java, DATABASE)
            .build()
    }

    @Provides
    fun provideDreamsDao(database: DreamDatabase): DreamsDao {
        return database.dreamsDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DreamBindingsModule {

    @Binds
    @Singleton
    abstract fun bindDreamRepository(impl: DreamRepositoryImpl): DreamRepository

    @Binds
    @Singleton
    abstract fun bindDreamGeminiRepository(impl: DreamGeminiRepositoryImpl): DreamGeminiRepository
}
