package com.whiplash.domain.usecase.auth

import com.whiplash.domain.entity.auth.request.LoginRequestEntity
import com.whiplash.domain.entity.auth.response.LoginResponseEntity
import com.whiplash.domain.repository.login.AuthRepository
import kotlinx.coroutines.flow.Flow

class SocialLoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: LoginRequestEntity): Flow<Result<LoginResponseEntity>> =
        repository.socialLogin(request)
}