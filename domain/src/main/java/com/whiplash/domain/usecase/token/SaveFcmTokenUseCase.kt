package com.whiplash.domain.usecase.token

import com.whiplash.domain.repository.token.TokenRepository

class SaveFcmTokenUseCase(
    private val repository: TokenRepository
) {
    suspend operator fun invoke(token: String) = repository.saveFcmToken(token)
}