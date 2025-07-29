package com.whiplash.data.di

import com.whiplash.data.repository.alarm.AlarmRepositoryImpl
import com.whiplash.data.repository.auth.AuthRepositoryImpl
import com.whiplash.data.repository.login.GoogleAuthRepositoryImpl
import com.whiplash.data.repository.login.KakaoAuthRepositoryImpl
import com.whiplash.data.repository.member.MemberRepositoryImpl
import com.whiplash.domain.repository.alarm.AlarmRepository
import com.whiplash.domain.repository.login.AuthRepository
import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.repository.login.KakaoAuthRepository
import com.whiplash.domain.repository.member.MemberRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindGoogleAuthRepository(impl: GoogleAuthRepositoryImpl): GoogleAuthRepository

    @Binds
    abstract fun bindKakaoAuthRepository(impl: KakaoAuthRepositoryImpl): KakaoAuthRepository

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository

    @Binds
    abstract fun bindMemberRepository(impl: MemberRepositoryImpl): MemberRepository

}