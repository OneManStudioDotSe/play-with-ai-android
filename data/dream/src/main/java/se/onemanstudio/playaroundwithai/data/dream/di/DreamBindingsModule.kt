package se.onemanstudio.playaroundwithai.data.dream.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.data.dream.data.repository.DreamGeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.data.dream.data.repository.DreamRepositoryImpl
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamGeminiRepository
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import javax.inject.Singleton

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
