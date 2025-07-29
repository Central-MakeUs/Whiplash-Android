package com.whiplash.domain.entity.auth.response

data class GoogleUserEntity(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val name: String? = null,
)