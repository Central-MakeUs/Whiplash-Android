package com.whiplash.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestTurnOffAlarm(
    val clientNow: String,
)
