package com.whiplash.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestInvokeSocialLogout(
    val deviceId: String,
)
