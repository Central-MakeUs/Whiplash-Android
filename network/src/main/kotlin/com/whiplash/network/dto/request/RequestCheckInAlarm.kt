package com.whiplash.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestCheckInAlarm(
    val latitude: Double,
    val longitude: Double
)