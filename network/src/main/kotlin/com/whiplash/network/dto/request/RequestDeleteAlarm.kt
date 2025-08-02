package com.whiplash.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestDeleteAlarm(
    val reason: String
)
