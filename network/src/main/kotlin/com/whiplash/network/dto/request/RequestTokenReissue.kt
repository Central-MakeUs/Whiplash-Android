package com.whiplash.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestTokenReissue(
    val deviceId: String,
)