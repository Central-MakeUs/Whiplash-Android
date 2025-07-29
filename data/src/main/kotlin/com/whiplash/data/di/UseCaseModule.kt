package com.whiplash.data.di

import com.whiplash.domain.repository.alarm.AlarmRepository
import com.whiplash.domain.repository.login.AuthRepository
import com.whiplash.domain.usecase.alarm.AddAlarmUseCase
import com.whiplash.domain.usecase.alarm.CreateAlarmOccurrenceUseCase
import com.whiplash.domain.usecase.alarm.GetAlarmsUseCase
import com.whiplash.domain.usecase.auth.ReissueTokenUseCase
import com.whiplash.domain.usecase.auth.SocialLoginUseCase
import com.whiplash.domain.usecase.auth.SocialLogoutUseCase
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

    @Provides
    @Singleton
    fun provideAddAlarmsUseCase(repository: AlarmRepository): AddAlarmUseCase =
        AddAlarmUseCase(repository)

    @Provides
    @Singleton
    fun provideCreateAlarmOccurrenceUseCase(repository: AlarmRepository): CreateAlarmOccurrenceUseCase =
        CreateAlarmOccurrenceUseCase(repository)

    @Provides
    @Singleton
    fun provideSocialLoginUseCase(repository: AuthRepository): SocialLoginUseCase =
        SocialLoginUseCase(repository)

    @Provides
    @Singleton
    fun provideReissueTokenUseCase(repository: AuthRepository): ReissueTokenUseCase =
        ReissueTokenUseCase(repository)

    @Provides
    @Singleton
    fun provideSocialLogoutUseCase(repository: AuthRepository): SocialLogoutUseCase =
        SocialLogoutUseCase(repository)

}