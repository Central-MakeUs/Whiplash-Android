package com.whiplash.data.mapper

import com.whiplash.domain.entity.auth.request.LoginRequestEntity
import com.whiplash.domain.entity.auth.request.LogoutRequestEntity
import com.whiplash.domain.entity.auth.request.TokenReissueRequestEntity
import com.whiplash.domain.entity.auth.response.LoginResponseEntity
import com.whiplash.domain.entity.auth.response.TokenReissueResponseEntity
import com.whiplash.network.dto.request.RequestInvokeSocialLogin
import com.whiplash.network.dto.request.RequestInvokeSocialLogout
import com.whiplash.network.dto.request.RequestTokenReissue
import com.whiplash.network.dto.response.InvokeSocialLoginResult
import com.whiplash.network.dto.response.TokenResult
import javax.inject.Inject

class AuthMapper @Inject constructor() {

    fun toSocialLoginRequest(entity: LoginRequestEntity): RequestInvokeSocialLogin {
        return RequestInvokeSocialLogin(
            socialType = entity.socialType,
            token = entity.token,
            deviceId = entity.deviceId
        )
    }

    fun toLoginResponseEntity(result: InvokeSocialLoginResult): LoginResponseEntity {
        return LoginResponseEntity(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            nickname = result.nickname,
            isNewMember = result.isNewMember
        )
    }

    fun toTokenReissueRequest(entity: TokenReissueRequestEntity): RequestTokenReissue {
        return RequestTokenReissue(
            deviceId = entity.deviceId,
        )
    }

    fun toTokenReissueResponseEntity(result: TokenResult): TokenReissueResponseEntity {
        return TokenReissueResponseEntity(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken
        )
    }

    fun toSocialLogoutRequest(entity: LogoutRequestEntity): RequestInvokeSocialLogout {
        return RequestInvokeSocialLogout(
            deviceId = entity.deviceId
        )
    }
}