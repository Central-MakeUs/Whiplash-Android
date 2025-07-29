package com.whiplash.domain.usecase.place

import com.whiplash.domain.entity.auth.response.PlaceDetailEntity
import com.whiplash.domain.repository.place.PlaceRepository
import kotlinx.coroutines.flow.Flow

class GetPlaceDetailUseCase(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Flow<Result<PlaceDetailEntity>> =
        repository.getPlaceDetail(latitude, longitude)
}