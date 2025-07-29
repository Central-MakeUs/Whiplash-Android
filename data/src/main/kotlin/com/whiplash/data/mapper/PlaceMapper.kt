package com.whiplash.data.mapper

import com.whiplash.domain.entity.auth.response.PlaceDetailEntity
import com.whiplash.domain.entity.auth.response.SearchPlaceEntity
import com.whiplash.network.dto.response.GetPlaceDetailResult
import com.whiplash.network.dto.response.SearchPlaceResult
import javax.inject.Inject

class PlaceMapper @Inject constructor() {

    fun toEntity(searchPlaceResult: SearchPlaceResult): SearchPlaceEntity {
        with(searchPlaceResult) {
            return SearchPlaceEntity(
                name = name,
                address = address,
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    fun toEntity(getPlaceDetailResult: GetPlaceDetailResult): PlaceDetailEntity {
        with(getPlaceDetailResult) {
            return PlaceDetailEntity(
                address = address,
                name = name
            )
        }
    }

}