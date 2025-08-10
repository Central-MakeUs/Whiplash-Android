package com.whiplash.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestRegisterFcmToken(
    val fcmToken: String,
)
