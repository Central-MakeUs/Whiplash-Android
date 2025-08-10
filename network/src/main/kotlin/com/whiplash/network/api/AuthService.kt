package com.whiplash.network.api

import com.whiplash.network.dto.request.RequestInvokeSocialLogin
import com.whiplash.network.dto.request.RequestInvokeSocialLogout
import com.whiplash.network.dto.request.RequestRegisterFcmToken
import com.whiplash.network.dto.request.RequestTokenReissue
import com.whiplash.network.dto.response.BaseResponse
import com.whiplash.network.dto.response.ResponseInvokeSocialLogin
import com.whiplash.network.dto.response.ResponseReissueToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    /**
     * 소셜 로그인
     *
     * @return accessToken, refreshToken
     */
    @POST("auth/social-login")
    suspend fun invokeSocialLogin(
        @Body requestInvokeSocialLogin: RequestInvokeSocialLogin
    ): Response<ResponseInvokeSocialLogin>

    /**
     * 토큰 재발급
     *
     * @return accessToken, refreshToken
     */
    @POST("auth/reissue")
    suspend fun reissueToken(
        @Body requestTokenReissue: RequestTokenReissue
    ): Response<ResponseReissueToken>

    /**
     * 로그아웃
     */
    @POST("auth/logout")
    suspend fun invokeSocialLogout(
        @Body requestInvokeSocialLogout: RequestInvokeSocialLogout
    ): Response<BaseResponse<Unit>>

    /**
     * FCM 토큰 등록
     */
    @POST("auth/fcm-token")
    suspend fun registerFcmToken(
        @Body requestRegisterFcmToken: RequestRegisterFcmToken
    ): Response<BaseResponse<Unit>>

}