package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseReissueToken(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: TokenResult
)

@Serializable
data class TokenResult(
    val accessToken: String,
    val refreshToken: String,
)