package com.whiplash.domain.repository.token

import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    val fcmToken: Flow<String?>
    suspend fun saveFcmToken(token: String)
    suspend fun clearFcmToken()
}