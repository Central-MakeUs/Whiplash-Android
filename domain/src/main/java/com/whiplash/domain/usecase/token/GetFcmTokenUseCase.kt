package com.whiplash.domain.usecase.token

import com.whiplash.domain.repository.token.TokenRepository
import kotlinx.coroutines.flow.Flow

class GetFcmTokenUseCase(
    private val repository: TokenRepository
) {
    operator fun invoke(): Flow<String?> = repository.fcmToken
}