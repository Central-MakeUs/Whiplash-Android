package com.whiplash.data.provider

import com.whiplash.data.datastore.TokenDataStore
import com.whiplash.domain.provider.TokenProvider
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class TokenProviderImpl @Inject constructor(
    private val tokenDataStore: TokenDataStore
): TokenProvider {

    override val accessToken: Flow<String?> = tokenDataStore.accessToken
    override val refreshToken: Flow<String?> = tokenDataStore.refreshToken
    override val deviceId: Flow<String?> = tokenDataStore.deviceId

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        Timber.d("## [data] 데이터 모듈에서 saveTokens() 호출. accessToken: $accessToken, refreshToken: $refreshToken")
        tokenDataStore.saveTokens(accessToken, refreshToken)
    }

    override suspend fun saveDeviceId(deviceId: String) {
        Timber.d("## [data] 데이터 모듈에서 saveDeviceId() 호출. deviceId: $deviceId")
        tokenDataStore.saveDeviceId(deviceId)
    }

    override suspend fun clearTokens() {
        Timber.d("## [data] 데이터 모듈에서 clearTokens() 호출")
        tokenDataStore.clearTokens()
    }
}