package com.whiplash.domain.entity

data class GoogleUserEntity(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val name: String? = null,
)