package com.whiplash.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestChangeTermsState(
    val privacyPolicy: Boolean,
    val pushNotificationPolicy: Boolean,
)