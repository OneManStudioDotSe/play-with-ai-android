package se.onemanstudio.playaroundwithai.data.chat.di

import android.content.Context
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
import se.onemanstudio.playaroundwithai.data.chat.data.repository.ChatGeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.data.chat.data.repository.PromptRepositoryImpl
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.ChatGeminiRepository
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.PromptRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatDataModule {

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
