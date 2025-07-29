package com.whiplash.domain.entity.auth.request

data class LoginRequestEntity(
    val socialType: String,
    val token: String,
    val deviceId: String,
)