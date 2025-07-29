package com.whiplash.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.whiplash.domain.provider.TokenProvider
import com.whiplash.network.api.AlarmService
import com.whiplash.network.api.AuthService
import com.whiplash.network.api.MemberService
import com.whiplash.network.api.PlaceService
import com.whiplash.network.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenReissueClient

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    // ====== JSON ======
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            prettyPrint = true
        }
    }

    // ====== 토큰 재발급용 ======
    @Provides
    @Singleton
    @TokenReissueClient
    fun provideTokenReissueOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @TokenReissueClient
    fun provideTokenReissueRetrofit(
        @TokenReissueClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://3.34.221.212:8080/api/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @TokenReissueClient
    fun provideTokenReissueAuthService(
        @TokenReissueClient retrofit: Retrofit
    ): AuthService = retrofit.create(AuthService::class.java)

    // ====== 일반용 ======
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenProvider: TokenProvider,
        @TokenReissueClient authService: AuthService
    ): AuthInterceptor = AuthInterceptor(tokenProvider, authService)

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://3.34.221.212:8080/api/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideAlarmService(retrofit: Retrofit): AlarmService =
        retrofit.create(AlarmService::class.java)

    @Provides
    @Singleton
    fun provideMemberService(retrofit: Retrofit): MemberService =
        retrofit.create(MemberService::class.java)

    @Provides
    @Singleton
    fun providePlaceService(retrofit: Retrofit): PlaceService =
        retrofit.create(PlaceService::class.java)
}