package com.whiplash.data.mapper

import com.whiplash.domain.entity.auth.request.LoginRequestEntity
import com.whiplash.domain.entity.auth.request.RegisterFcmTokenRequestEntity
import com.whiplash.domain.entity.auth.response.LoginResponseEntity
import com.whiplash.domain.entity.auth.response.TokenReissueResponseEntity
import com.whiplash.network.dto.request.RequestInvokeSocialLogin
import com.whiplash.network.dto.request.RequestRegisterFcmToken
import com.whiplash.network.dto.response.InvokeSocialLoginResult
import com.whiplash.network.dto.response.TokenResult
import javax.inject.Inject

class AuthMapper @Inject constructor() {

    fun toSocialLoginRequest(entity: LoginRequestEntity): RequestInvokeSocialLogin {
        with(entity) {
            return RequestInvokeSocialLogin(
                socialType = socialType,
                token = token,
                deviceId = deviceId
            )
        }
    }

    fun toLoginResponseEntity(result: InvokeSocialLoginResult): LoginResponseEntity {
        with(result) {
            return LoginResponseEntity(
                accessToken = accessToken,
                refreshToken = refreshToken,
                nickname = nickname,
                isNewMember = isNewMember
            )
        }
    }

    fun toTokenReissueResponseEntity(result: TokenResult): TokenReissueResponseEntity {
        return TokenReissueResponseEntity(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken
        )
    }

    fun toRegisterFcmTokenRequest(entity: RegisterFcmTokenRequestEntity): RequestRegisterFcmToken {
        return RequestRegisterFcmToken(
            fcmToken = entity.fcmToken
        )
    }

}