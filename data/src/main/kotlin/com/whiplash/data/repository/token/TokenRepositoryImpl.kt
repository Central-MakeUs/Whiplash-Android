package com.whiplash.data.repository.token

import com.whiplash.data.datastore.TokenDataStore
import com.whiplash.domain.repository.token.TokenRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TokenRepositoryImpl @Inject constructor(
    private val tokenDataStore: TokenDataStore
): TokenRepository {
    override val fcmToken: Flow<String?> = tokenDataStore.fcmToken

    override suspend fun saveFcmToken(token: String) = tokenDataStore.saveFcmToken(token)

    override suspend fun clearFcmToken() = tokenDataStore.clearTokens()
}