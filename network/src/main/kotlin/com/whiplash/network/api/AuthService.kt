package com.whiplash.network.api

import com.whiplash.network.dto.request.RequestTokenReissue
import com.whiplash.network.dto.response.ResponseReissueToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    /**
     * 토큰 재발급
     *
     * @return accessToken, refreshToken
     */
    @POST("auth/reissue")
    suspend fun reissueToken(
        @Body requestTokenReissue: RequestTokenReissue
    ): Response<ResponseReissueToken>

}