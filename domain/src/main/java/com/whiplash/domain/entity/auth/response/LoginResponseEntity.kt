package com.whiplash.domain.entity.auth.response

data class LoginResponseEntity(
    val accessToken: String,
    val refreshToken: String,
    val nickname: String,
    val isNewMember: Boolean
)