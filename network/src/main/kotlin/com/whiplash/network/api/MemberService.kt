package com.whiplash.network.api

import com.whiplash.network.dto.request.RequestChangeTermsState
import com.whiplash.network.dto.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

interface MemberService {

    /**
     * 회원 약관 동의 정보 변경
     */
    @PUT("members/terms")
    suspend fun changeTermsState(
        @Body requestChangeTermsState: RequestChangeTermsState
    ): Response<BaseResponse<Unit>>

}