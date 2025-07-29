package com.whiplash.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.alarm.request.AddAlarmRequest
import com.whiplash.domain.entity.alarm.response.CreateAlarmOccurrenceEntity
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.domain.provider.CrashlyticsProvider
import com.whiplash.domain.usecase.alarm.AddAlarmUseCase
import com.whiplash.domain.usecase.alarm.CreateAlarmOccurrenceUseCase
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
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val addAlarmUseCase: AddAlarmUseCase,
    private val createAlarmOccurrenceUseCase: CreateAlarmOccurrenceUseCase,
    private val crashlyticsProvider: CrashlyticsProvider,
) : ViewModel() {

    data class MainUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        // 알람 목록 조회 api 결과
        val alarmList: List<GetAlarmEntity> = emptyList(),

        val isAddAlarm: Boolean = false,

        // 알람 발생 내역 생성 결과
        val isCreateAlarmOccurrence: Boolean = false,
        val createdOccurrence: CreateAlarmOccurrenceEntity? = null,
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
                    crashlyticsProvider.recordError(e)
                    crashlyticsProvider.logError("알림 목록 조회 api 실패 : ${e.message}")
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
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("알림 목록 조회 api 실패 : ${e.message}")
            Timber.e("## [알람 목록 조회] 에러 : $e")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    // 알람 등록
    fun addAlarm(request: AddAlarmRequest) = viewModelScope.launch {
        _uiState.update { it.copy(isAddAlarm = true) }

        try {
            addAlarmUseCase(request).collect { result ->
                result.onSuccess {
                    Timber.d("## [알람 등록] 성공")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAddAlarm = false,
                            errorMessage = null
                        )
                    }
                    getAlarms()
                }.onFailure { e ->
                    crashlyticsProvider.recordError(e)
                    crashlyticsProvider.logError("알람 등록 api 실패 : ${e.message}")
                    Timber.e("## [알람 등록] 실패 : $e")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAddAlarm = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("알람 등록 api 에러 : ${e.message}")
            Timber.e("## [알람 등록] 에러 : $e")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAddAlarm = false,
                    errorMessage = e.message
                )
            }
        }
    }

    // 알람 발생 내역 생성
    fun createAlarmOccurrence(alarmId: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isCreateAlarmOccurrence = true) }

        try {
            createAlarmOccurrenceUseCase(alarmId).collect { result ->
                result.onSuccess { occurrence ->
                    Timber.d("## [알람 발생 내역 생성] 성공 : $occurrence")
                    _uiState.update {
                        it.copy(
                            isCreateAlarmOccurrence = false,
                            createdOccurrence = occurrence,
                            errorMessage = null
                        )
                    }
                }.onFailure { e ->
                    crashlyticsProvider.recordError(e)
                    crashlyticsProvider.logError("알람 발생 내역 생성 api 실패 : ${e.message}")
                    Timber.e("## [알람 발생 내역 생성] 실패 : $e")
                    _uiState.update {
                        it.copy(
                            isCreateAlarmOccurrence = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("알람 발생 내역 생성 api 에러 : ${e.message}")
            Timber.e("## [알람 발생 내역 생성] 에러 : $e")
            _uiState.update {
                it.copy(
                    isCreateAlarmOccurrence = false,
                    errorMessage = e.message
                )
            }
        }
    }

}