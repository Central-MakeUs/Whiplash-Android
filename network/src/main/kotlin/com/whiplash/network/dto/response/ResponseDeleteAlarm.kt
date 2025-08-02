package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseDeleteAlarm(
    val reason: String,
)
