package com.whiplash.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.GetAlarmEntity
import com.whiplash.domain.usecase.alarm.GetAlarmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAlarmsUseCase: GetAlarmsUseCase
): ViewModel() {

    data class MainUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        // 알람 목록 조회 api 결과
        val alarmList: List<GetAlarmEntity> = emptyList(),
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // 알람 목록 조회
    fun getAlarms() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        try {
            getAlarmsUseCase().collect { result ->
                result.onSuccess { alarms ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alarmList = alarms,
                            errorMessage = null
                        )
                    }
                }.onFailure { e ->
                    Timber.e("## [알람 목록 조회] 실패 : $e")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("## [알람 목록 조회] 에러 : $e")
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

}