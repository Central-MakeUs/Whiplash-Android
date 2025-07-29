package com.whiplash.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestInvokeSocialLogin(
    val socialType: String, // GOOGLE
    val token: String,
    val deviceId: String,
)