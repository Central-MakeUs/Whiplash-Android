package com.whiplash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.auth.request.RegisterFcmTokenRequestEntity
import com.whiplash.domain.provider.CrashlyticsProvider
import com.whiplash.domain.usecase.auth.RegisterFcmTokenUseCase
import com.whiplash.domain.usecase.onboarding.GetOnboardingStatusUseCase
import com.whiplash.domain.usecase.token.GetFcmTokenUseCase
import com.whiplash.domain.usecase.token.SaveFcmTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val saveFcmTokenUseCase: SaveFcmTokenUseCase,
    private val getFcmTokenUseCase: GetFcmTokenUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val crashlyticsProvider: CrashlyticsProvider,
) : ViewModel() {

    data class SplashUiState(
        val isFcmTokenSaved: Boolean = false,
        val currentFcmToken: String? = null,
        val errorMessage: String? = null,
    )

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    val isOnboardingCompleted = getOnboardingStatusUseCase()

    fun saveFcmToken(token: String) = viewModelScope.launch {
        try {
            saveFcmTokenUseCase(token)
            Timber.d("## [FCM] FCM 토큰 저장 완료 : $token")
            _uiState.update { it.copy(isFcmTokenSaved = true) }
        } catch (e: Exception) {
            Timber.e(e, "## [FCM] FCM 토큰 저장 실패")
            _uiState.update {
                it.copy(errorMessage = "FCM 토큰 저장 실패: ${e.message}")
            }
        }
    }

    fun getCurrentFcmToken() = viewModelScope.launch {
        getFcmTokenUseCase().collect { token ->
            _uiState.update { it.copy(currentFcmToken = token) }
            Timber.d("## [FCM] 현재 저장된 FCM 토큰: $token")
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    // FCM 토큰 등록
    fun registerFcmToken(fcmToken: String) = viewModelScope.launch {
        try {
            val request = RegisterFcmTokenRequestEntity(
                fcmToken = fcmToken
            )
            registerFcmTokenUseCase.invoke(request).collect { result ->
                result.onSuccess {
                    Timber.d("## [FCM 토큰 등록] 성공")
                    _uiState.update {
                        it.copy(
                            isFcmTokenSaved = true,
                            errorMessage = null
                        )
                    }
                }.onFailure { e ->
                    Timber.e("## [FCM 토큰 등록] 실패")
                    crashlyticsProvider.recordError(e)
                    crashlyticsProvider.logError("FCM 토큰 등록 api 실패 : ${e.message}")
                    _uiState.update {
                        it.copy(
                            isFcmTokenSaved = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("## [FCM 토큰 등록] 에러")
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("FCM 토큰 등록 api 에러 : ${e.message}")
            _uiState.update {
                it.copy(
                    isFcmTokenSaved = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearIsFcmTokenSaved() = _uiState.update { it.copy(isFcmTokenSaved = false) }

}