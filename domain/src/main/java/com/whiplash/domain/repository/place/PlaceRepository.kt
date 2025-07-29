package com.whiplash.domain.repository.place

import com.whiplash.domain.entity.auth.response.PlaceDetailEntity
import com.whiplash.domain.entity.auth.response.SearchPlaceEntity
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    suspend fun searchPlace(query: String): Flow<Result<List<SearchPlaceEntity>>>
    suspend fun getPlaceDetail(latitude: Double, longitude: Double): Flow<Result<PlaceDetailEntity>>
}