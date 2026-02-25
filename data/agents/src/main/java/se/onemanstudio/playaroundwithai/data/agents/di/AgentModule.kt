package se.onemanstudio.playaroundwithai.data.agents.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.data.agents.data.repository.TripPlannerRepositoryImpl
import se.onemanstudio.playaroundwithai.data.agents.domain.repository.TripPlannerRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AgentModule {

    @Binds
    @Singleton
    abstract fun bindTripPlannerRepository(impl: TripPlannerRepositoryImpl): TripPlannerRepository
}
