package com.whiplash.domain.usecase.place

import com.whiplash.domain.entity.auth.response.SearchPlaceEntity
import com.whiplash.domain.repository.place.PlaceRepository
import kotlinx.coroutines.flow.Flow

class SearchPlaceUseCase(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(query: String): Flow<Result<List<SearchPlaceEntity>>> =
        repository.searchPlace(query)
}