package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseReissueToken = BaseResponse<TokenResult>

@Serializable
data class TokenResult(
    val accessToken: String,
    val refreshToken: String,
)