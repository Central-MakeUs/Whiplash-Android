package com.whiplash.domain.provider

import kotlinx.coroutines.flow.Flow

interface TokenProvider {
    val accessToken: Flow<String?>
    val refreshToken: Flow<String?>
    val deviceId: Flow<String?>
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun saveDeviceId(deviceId: String)
    suspend fun clearTokens()
}