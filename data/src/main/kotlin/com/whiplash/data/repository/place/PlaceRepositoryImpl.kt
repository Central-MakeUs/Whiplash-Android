package com.whiplash.data.repository.place

import com.whiplash.data.mapper.PlaceMapper
import com.whiplash.data.repository.safeApiCallWithTransform
import com.whiplash.domain.entity.auth.response.PlaceDetailEntity
import com.whiplash.domain.entity.auth.response.SearchPlaceEntity
import com.whiplash.domain.repository.place.PlaceRepository
import com.whiplash.network.api.PlaceService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val placeService: PlaceService,
    private val placeMapper: PlaceMapper,
): PlaceRepository {
    override suspend fun searchPlace(query: String): Flow<Result<List<SearchPlaceEntity>>> =
        safeApiCallWithTransform(
            apiCall = { placeService.searchPlace(query) },
            transform = { response -> response.result.map { placeMapper.toEntity(it) } }
        )

    override suspend fun getPlaceDetail(latitude: Double, longitude: Double): Flow<Result<PlaceDetailEntity>> =
        safeApiCallWithTransform(
            apiCall = { placeService.getPlaceDetail(latitude, longitude) },
            transform = { response -> placeMapper.toEntity(response.result) }
        )
}