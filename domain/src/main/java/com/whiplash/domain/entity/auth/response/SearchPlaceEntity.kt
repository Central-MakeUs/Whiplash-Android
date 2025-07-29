package com.whiplash.domain.entity.auth.response

data class SearchPlaceEntity(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)