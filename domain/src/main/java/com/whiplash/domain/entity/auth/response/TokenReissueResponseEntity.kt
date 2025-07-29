package com.whiplash.domain.entity.auth.response

data class TokenReissueResponseEntity(
    val accessToken: String,
    val refreshToken: String
)