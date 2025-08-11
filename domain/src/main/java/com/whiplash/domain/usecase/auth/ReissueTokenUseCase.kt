package com.whiplash.domain.usecase.auth

import com.whiplash.domain.entity.auth.response.TokenReissueResponseEntity
import com.whiplash.domain.repository.login.AuthRepository
import kotlinx.coroutines.flow.Flow

class ReissueTokenUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Flow<Result<TokenReissueResponseEntity>> = repository.reissueToken()
}