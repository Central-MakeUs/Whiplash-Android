package com.whiplash.domain.usecase.auth

import com.whiplash.domain.entity.auth.request.RegisterFcmTokenRequestEntity
import com.whiplash.domain.repository.login.AuthRepository
import kotlinx.coroutines.flow.Flow

class RegisterFcmTokenUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: RegisterFcmTokenRequestEntity): Flow<Result<Unit>> =
        repository.registerFcmToken(request)
}