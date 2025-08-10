package com.whiplash.data.di

import com.whiplash.domain.repository.alarm.AlarmRepository
import com.whiplash.domain.repository.login.AuthRepository
import com.whiplash.domain.repository.member.MemberRepository
import com.whiplash.domain.repository.onboarding.OnboardingRepository
import com.whiplash.domain.repository.place.PlaceRepository
import com.whiplash.domain.usecase.alarm.AddAlarmUseCase
import com.whiplash.domain.usecase.alarm.CheckInAlarmUseCase
import com.whiplash.domain.usecase.alarm.CreateAlarmOccurrenceUseCase
import com.whiplash.domain.usecase.alarm.DeleteAlarmUseCase
import com.whiplash.domain.usecase.alarm.GetAlarmsUseCase
import com.whiplash.domain.usecase.alarm.TurnOffAlarmUseCase
import com.whiplash.domain.usecase.auth.ReissueTokenUseCase
import com.whiplash.domain.usecase.auth.SocialLoginUseCase
import com.whiplash.domain.usecase.auth.SocialLogoutUseCase
import com.whiplash.domain.usecase.member.ChangeTermsStateUseCase
import com.whiplash.domain.usecase.member.WithdrawUseCase
import com.whiplash.domain.usecase.onboarding.GetOnboardingStatusUseCase
import com.whiplash.domain.usecase.onboarding.SetOnboardingCompletedUseCase
import com.whiplash.domain.usecase.place.GetPlaceDetailUseCase
import com.whiplash.domain.usecase.place.SearchPlaceUseCase
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

    @Provides
    @Singleton
    fun provideWithdrawUseCase(repository: MemberRepository): WithdrawUseCase =
        WithdrawUseCase(repository)

    @Provides
    @Singleton
    fun provideChangeTermsStateUseCase(repository: MemberRepository): ChangeTermsStateUseCase =
        ChangeTermsStateUseCase(repository)

    @Provides
    @Singleton
    fun provideSearchPlaceUseCase(repository: PlaceRepository): SearchPlaceUseCase =
        SearchPlaceUseCase(repository)

    @Provides
    @Singleton
    fun provideGetPlaceDetailUseCase(repository: PlaceRepository): GetPlaceDetailUseCase =
        GetPlaceDetailUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteAlarmUseCase(repository: AlarmRepository): DeleteAlarmUseCase =
        DeleteAlarmUseCase(repository)

    @Provides
    @Singleton
    fun provideTurnOffAlarmUseCase(repository: AlarmRepository): TurnOffAlarmUseCase =
        TurnOffAlarmUseCase(repository)

    @Provides
    @Singleton
    fun provideCheckInAlarmUseCase(repository: AlarmRepository): CheckInAlarmUseCase =
        CheckInAlarmUseCase(repository)

    @Provides
    @Singleton
    fun provideSetOnboardingCompletedUseCase(repository: OnboardingRepository): SetOnboardingCompletedUseCase =
        SetOnboardingCompletedUseCase(repository)

    @Provides
    @Singleton
    fun provideGetOnboardingStatusUseCase(repository: OnboardingRepository): GetOnboardingStatusUseCase =
        GetOnboardingStatusUseCase(repository)

}