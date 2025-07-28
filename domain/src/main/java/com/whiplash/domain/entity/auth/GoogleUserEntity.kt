package com.whiplash.domain.entity.auth

data class GoogleUserEntity(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val name: String? = null,
)