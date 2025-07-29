package com.whiplash.domain.entity.auth.request

data class ChangeTermsStateEntity(
    val privacyPolicy: Boolean,
    val pushNotificationPolicy: Boolean,
)