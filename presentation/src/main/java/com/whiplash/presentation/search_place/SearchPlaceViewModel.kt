package com.whiplash.presentation.search_place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.auth.response.PlaceDetailEntity
import com.whiplash.domain.entity.auth.response.SearchPlaceEntity
import com.whiplash.domain.provider.CrashlyticsProvider
import com.whiplash.domain.usecase.place.GetPlaceDetailUseCase
import com.whiplash.domain.usecase.place.SearchPlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchPlaceViewModel @Inject constructor(
    private val searchPlaceUseCase: SearchPlaceUseCase,
    private val getPlaceDetailUseCase: GetPlaceDetailUseCase,
    private val crashlyticsProvider: CrashlyticsProvider,
): ViewModel() {

    data class SearchPlaceUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        // 장소 검색 결과
        val placeList: List<SearchPlaceEntity> = emptyList(),

        // 장소 상세 조회 결과
        val placeDetail: PlaceDetailEntity? = null,
    )

    private val _uiState = MutableStateFlow(SearchPlaceUiState())
    val uiState: StateFlow<SearchPlaceUiState> = _uiState.asStateFlow()

    // 장소 검색
    fun searchPlace(query: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        try {
            searchPlaceUseCase.invoke(query).collect { result ->
                result.onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            placeList = response
                        )
                    }
                }.onFailure { e ->
                    crashlyticsProvider.recordError(e)
                    crashlyticsProvider.logError("장소 검색 api 실패 : ${e.message}")
                    Timber.e("## [장소 검색] 실패 : $e")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("장소 검색 api 에러 : ${e.message}")
            Timber.e("## [장소 검색] 에러 : $e")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    // 장소 상세 조회
    fun getPlaceDetail(
        latitude: Double,
        longitude: Double,
    ) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        try {
            getPlaceDetailUseCase.invoke(latitude, longitude).collect { result ->
                result.onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            placeDetail = response
                        )
                    }
                }.onFailure { e ->
                    crashlyticsProvider.recordError(e)
                    crashlyticsProvider.logError("장소 검색 api 실패 : ${e.message}")
                    Timber.e("## [장소 검색] 실패 : $e")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("장소 검색 api 에러 : ${e.message}")
            Timber.e("## [장소 검색] 에러 : $e")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

}