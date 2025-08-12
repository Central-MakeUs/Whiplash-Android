package com.whiplash.domain.usecase.token

import com.whiplash.domain.repository.token.TokenRepository

class ClearFcmTokenUseCase(
    private val repository: TokenRepository
) {
    suspend operator fun invoke() = repository.clearFcmToken()
}