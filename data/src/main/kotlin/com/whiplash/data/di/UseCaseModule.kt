package com.whiplash.data.di

import com.whiplash.domain.repository.alarm.AlarmRepository
import com.whiplash.domain.repository.alarm.DisabledAlarmRepository
import com.whiplash.domain.repository.login.AuthRepository
import com.whiplash.domain.repository.member.MemberRepository
import com.whiplash.domain.repository.onboarding.OnboardingRepository
import com.whiplash.domain.repository.place.PlaceRepository
import com.whiplash.domain.repository.token.TokenRepository
import com.whiplash.domain.usecase.alarm.AddAlarmUseCase
import com.whiplash.domain.usecase.alarm.CheckInAlarmUseCase
import com.whiplash.domain.usecase.alarm.CreateAlarmOccurrenceUseCase
import com.whiplash.domain.usecase.alarm.DeleteAlarmUseCase
import com.whiplash.domain.usecase.alarm.GetAlarmDisabledUseCase
import com.whiplash.domain.usecase.alarm.GetAlarmsUseCase
import com.whiplash.domain.usecase.alarm.GetRemainingDisableCountUseCase
import com.whiplash.domain.usecase.alarm.SetAlarmDisabledUseCase
import com.whiplash.domain.usecase.alarm.TurnOffAlarmUseCase
import com.whiplash.domain.usecase.auth.RegisterFcmTokenUseCase
import com.whiplash.domain.usecase.auth.ReissueTokenUseCase
import com.whiplash.domain.usecase.auth.SocialLoginUseCase
import com.whiplash.domain.usecase.auth.SocialLogoutUseCase
import com.whiplash.domain.usecase.member.ChangeTermsStateUseCase
import com.whiplash.domain.usecase.member.WithdrawUseCase
import com.whiplash.domain.usecase.onboarding.GetOnboardingStatusUseCase
import com.whiplash.domain.usecase.onboarding.SetOnboardingCompletedUseCase
import com.whiplash.domain.usecase.place.GetPlaceDetailUseCase
import com.whiplash.domain.usecase.place.SearchPlaceUseCase
import com.whiplash.domain.usecase.token.ClearFcmTokenUseCase
import com.whiplash.domain.usecase.token.GetFcmTokenUseCase
import com.whiplash.domain.usecase.token.SaveFcmTokenUseCase
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
    fun provideRegisterFcmTokenUseCase(repository: AuthRepository): RegisterFcmTokenUseCase =
        RegisterFcmTokenUseCase(repository)

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
    fun provideGetRemainingDisableCountUseCase(repository: AlarmRepository): GetRemainingDisableCountUseCase =
        GetRemainingDisableCountUseCase(repository)

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

    @Provides
    @Singleton
    fun provideSaveFcmTokenUseCase(repository: TokenRepository): SaveFcmTokenUseCase =
        SaveFcmTokenUseCase(repository)

    @Provides
    @Singleton
    fun provideGetFcmTokenUseCase(repository: TokenRepository): GetFcmTokenUseCase =
        GetFcmTokenUseCase(repository)

    @Provides
    @Singleton
    fun provideClearFcmTokenUseCase(repository: TokenRepository): ClearFcmTokenUseCase =
        ClearFcmTokenUseCase(repository)

    @Provides
    @Singleton
    fun provideSetAlarmDisabledUseCase(repository: DisabledAlarmRepository): SetAlarmDisabledUseCase =
        SetAlarmDisabledUseCase(repository)

    @Provides
    @Singleton
    fun provideGetAlarmDisabledUseCase(repository: DisabledAlarmRepository): GetAlarmDisabledUseCase =
        GetAlarmDisabledUseCase(repository)

}