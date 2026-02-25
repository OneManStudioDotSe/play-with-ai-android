package se.onemanstudio.playaroundwithai.data.plan.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.data.plan.data.repository.TripPlannerRepositoryImpl
import se.onemanstudio.playaroundwithai.data.plan.domain.repository.TripPlannerRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlanModule {

    @Binds
    @Singleton
    abstract fun bindTripPlannerRepository(impl: TripPlannerRepositoryImpl): TripPlannerRepository
}
