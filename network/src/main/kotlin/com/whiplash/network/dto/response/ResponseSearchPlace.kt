package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseSearchPlace = BaseResponse<List<SearchPlaceResult>>

@Serializable
data class SearchPlaceResult(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)