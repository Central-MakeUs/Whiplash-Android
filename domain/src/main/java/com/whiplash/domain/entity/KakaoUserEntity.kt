package com.whiplash.domain.entity

data class KakaoLoginResult(
    val accessToken: String,
    val user: KakaoUserEntity
)

data class KakaoUserEntity(
    val id: String,
    val email: String?,
    val nickname: String?,
    val profileImageUrl: String?
)