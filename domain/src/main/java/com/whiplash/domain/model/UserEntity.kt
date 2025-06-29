package com.whiplash.domain.model

data class UserEntity(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)