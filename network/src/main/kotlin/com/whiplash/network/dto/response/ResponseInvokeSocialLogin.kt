package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseInvokeSocialLogin = BaseResponse<InvokeSocialLoginResult>

@Serializable
data class InvokeSocialLoginResult(
    val accessToken: String,
    val refreshToken: String,
    val nickname: String,
    val isNewMember: Boolean,
)