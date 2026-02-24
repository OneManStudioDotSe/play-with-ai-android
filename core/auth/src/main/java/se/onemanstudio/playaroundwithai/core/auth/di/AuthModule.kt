package se.onemanstudio.playaroundwithai.core.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
