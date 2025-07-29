package com.whiplash.data.repository.auth

import com.whiplash.data.mapper.AuthMapper
import com.whiplash.data.repository.safeApiCallWithTransform
import com.whiplash.domain.entity.auth.request.LoginRequestEntity
import com.whiplash.domain.entity.auth.request.LogoutRequestEntity
import com.whiplash.domain.entity.auth.request.TokenReissueRequestEntity
import com.whiplash.domain.entity.auth.response.LoginResponseEntity
import com.whiplash.domain.entity.auth.response.TokenReissueResponseEntity
import com.whiplash.domain.repository.login.AuthRepository
import com.whiplash.network.api.AuthService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val authMapper: AuthMapper,
): AuthRepository {
    override suspend fun socialLogin(request: LoginRequestEntity): Flow<Result<LoginResponseEntity>> =
        safeApiCallWithTransform(
            apiCall = { authService.invokeSocialLogin(authMapper.toSocialLoginRequest(request)) },
            transform = { response -> authMapper.toLoginResponseEntity(response.result) }
        )

    override suspend fun reissueToken(request: TokenReissueRequestEntity): Flow<Result<TokenReissueResponseEntity>> =
        safeApiCallWithTransform(
            apiCall = { authService.reissueToken(authMapper.toTokenReissueRequest(request)) },
            transform = { response -> authMapper.toTokenReissueResponseEntity(response.result) }
        )

    override suspend fun socialLogout(request: LogoutRequestEntity): Flow<Result<Unit>> =
        safeApiCallWithTransform(
            apiCall = { authService.invokeSocialLogout(authMapper.toSocialLogoutRequest(request)) },
            transform = { Unit }
        )
}