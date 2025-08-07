package com.whiplash.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.usecase.login.google.GetCurrentUserUseCase
import com.whiplash.domain.usecase.login.kakao.GetCurrentKakaoUserUseCase
import com.whiplash.domain.usecase.onboarding.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentKakaoUserUseCase: GetCurrentKakaoUserUseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    fun completeOnboarding() = viewModelScope.launch {
        setOnboardingCompletedUseCase()
        
        // 로그인 상태 체크
        val isLoggedIn = checkLoginStatus()
        
        if (isLoggedIn) {
            _navigationEvent.emit(NavigationEvent.NavigateToMain)
        } else {
            _navigationEvent.emit(NavigationEvent.NavigateToLogin)
        }
    }

    private suspend fun checkLoginStatus(): Boolean {
        // 구글 로그인 상태 체크
        val googleUser = getCurrentUserUseCase()
        if (googleUser != null) return true
        
        // 카카오 로그인 상태 체크
        val kakaoUserResult = getCurrentKakaoUserUseCase()
        return kakaoUserResult.isSuccess && kakaoUserResult.getOrNull() != null
    }

    sealed class NavigationEvent {
        data object NavigateToMain : NavigationEvent()
        data object NavigateToLogin : NavigationEvent()
    }
}