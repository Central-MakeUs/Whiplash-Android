package com.whiplash.data.repository.auth

import com.whiplash.data.mapper.AuthMapper
import com.whiplash.data.repository.safeApiCallWithTransform
import com.whiplash.domain.entity.auth.request.LoginRequestEntity
import com.whiplash.domain.entity.auth.request.LogoutRequestEntity
import com.whiplash.domain.entity.auth.request.RegisterFcmTokenRequestEntity
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
            transform = { response ->
                response.result?.let { authMapper.toLoginResponseEntity(it) }
                    ?: throw Exception("소셜로그인 api 응답이 null")
            }
        )

    override suspend fun reissueToken(request: TokenReissueRequestEntity): Flow<Result<TokenReissueResponseEntity>> =
        safeApiCallWithTransform(
            apiCall = { authService.reissueToken(authMapper.toTokenReissueRequest(request)) },
            transform = { response ->
                response.result?.let { authMapper.toTokenReissueResponseEntity(it) }
                    ?: throw Exception("토큰 재발행 api 응답이 null")
            }
        )

    override suspend fun socialLogout(request: LogoutRequestEntity): Flow<Result<Unit>> =
        safeApiCallWithTransform(
            apiCall = { authService.invokeSocialLogout(authMapper.toSocialLogoutRequest(request)) },
            transform = { Unit }
        )

    override suspend fun registerFcmToken(request: RegisterFcmTokenRequestEntity): Flow<Result<Unit>> =
        safeApiCallWithTransform(
            apiCall = { authService.registerFcmToken(authMapper.toRegisterFcmTokenRequest(request)) },
            transform = { Unit }
        )
}