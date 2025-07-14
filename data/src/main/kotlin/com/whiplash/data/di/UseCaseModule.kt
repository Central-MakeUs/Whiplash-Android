package com.whiplash.data.di

import com.whiplash.domain.repository.alarm.AlarmRepository
import com.whiplash.domain.usecase.alarm.GetAlarmsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetAlarmsUseCase(repository: AlarmRepository): GetAlarmsUseCase =
        GetAlarmsUseCase(repository)

}