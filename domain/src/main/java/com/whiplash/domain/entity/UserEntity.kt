package com.whiplash.domain.entity

data class UserEntity(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val name: String? = null,
)