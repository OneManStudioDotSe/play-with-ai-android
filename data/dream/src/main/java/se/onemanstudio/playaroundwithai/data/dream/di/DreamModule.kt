package se.onemanstudio.playaroundwithai.data.dream.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.data.dream.data.repository.DreamGeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.data.dream.data.repository.DreamRepositoryImpl
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamGeminiRepository
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import java.io.File
import javax.inject.Singleton

private const val DREAM_IMAGES_DIR = "dream_images"

@Module
@InstallIn(SingletonComponent::class)
abstract class DreamModule {

    @Binds
    @Singleton
    abstract fun bindDreamRepository(impl: DreamRepositoryImpl): DreamRepository

    @Binds
    @Singleton
    abstract fun bindDreamGeminiRepository(impl: DreamGeminiRepositoryImpl): DreamGeminiRepository

    companion object {
        @Provides
        @DreamImagesDir
        fun provideDreamImagesDir(@ApplicationContext context: Context): File =
            File(context.filesDir, DREAM_IMAGES_DIR)
    }
}
