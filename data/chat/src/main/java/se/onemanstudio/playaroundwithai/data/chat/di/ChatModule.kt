package se.onemanstudio.playaroundwithai.data.chat.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.data.chat.data.repository.ChatGeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.data.chat.data.repository.PromptRepositoryImpl
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.ChatGeminiRepository
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.PromptRepository
import javax.inject.Singleton

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
