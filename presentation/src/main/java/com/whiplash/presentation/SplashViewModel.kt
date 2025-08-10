package com.whiplash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val getFcmTokenUseCase: GetFcmTokenUseCase
) : ViewModel() {

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
}

data class SplashUiState(
    val isFcmTokenSaved: Boolean = false,
    val currentFcmToken: String? = null,
    val errorMessage: String? = null
)