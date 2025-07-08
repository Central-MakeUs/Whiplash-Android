package com.whiplash.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.KakaoUserEntity
import com.whiplash.domain.usecase.login.kakao.GetCurrentKakaoUserUseCase
import com.whiplash.domain.usecase.login.kakao.SignInWithKakaoUseCase
import com.whiplash.domain.usecase.login.kakao.SignOutKakaoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class KakaoLoginViewModel @Inject constructor(
    private val signInWithKakaoUseCase: SignInWithKakaoUseCase,
    private val signOutKakaoUseCase: SignOutKakaoUseCase,
    private val getCurrentKakaoUserUseCase: GetCurrentKakaoUserUseCase,
): ViewModel() {

    private val _uiState = MutableStateFlow(KakaoLoginUiState())
    val uiState: StateFlow<KakaoLoginUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() = viewModelScope.launch(Dispatchers.IO) {
        try {
            getCurrentKakaoUserUseCase()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            isSignIn = (user != null)
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e("## [카카오 로그인] 현재 사용자 확인 실패: ${e.message}")
                    _uiState.update {
                        it.copy(
                            user = null,
                            isSignIn = false
                        )
                    }
                }
        } catch (e: Exception) {
            Timber.e("## [카카오 로그인] 현재 사용자 확인 에러: ${e.message}")
            _uiState.update {
                it.copy(
                    user = null,
                    isSignIn = false
                )
            }
        }
    }

    /**
     * 카카오 로그인 시작 (UI 상태만 업데이트)
     */
    fun startKakaoLogin() {
        _uiState.update { it.copy(isLoading = true) }
    }

    /**
     * 카카오 로그인 성공 시 토큰으로 사용자 정보 조회
     */
    fun handleKakaoLoginSuccess(accessToken: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            signInWithKakaoUseCase(accessToken)
                .onSuccess { loginResult ->
                    Timber.d("## [카카오 로그인] 로그인 성공. 유저 정보: ${loginResult.user}")
                    _uiState.update {
                        it.copy(
                            user = loginResult.user,
                            accessToken = loginResult.accessToken,
                            isSignIn = true,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e("## [카카오 로그인] 사용자 정보 조회 실패: ${e.message}")
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "카카오 로그인에 실패했습니다",
                            isLoading = false
                        )
                    }
                }
        } catch (e: Exception) {
            Timber.e("## [카카오 로그인] 로그인 에러: ${e.message}")
            _uiState.update {
                it.copy(
                    error = e.message ?: "카카오 로그인에 실패했습니다",
                    isLoading = false
                )
            }
        }
    }

    /**
     * 카카오 로그인 실패 처리
     */
    fun handleKakaoLoginFailure(errorMessage: String) {
        Timber.e("## [카카오 로그인] 로그인 실패: $errorMessage")
        _uiState.update {
            it.copy(
                error = errorMessage,
                isLoading = false
            )
        }
    }

    fun signOut() = viewModelScope.launch(Dispatchers.IO) {
        try {
            // 이미 로그아웃된 상태라면 바로 처리
            if (!_uiState.value.isSignIn) {
                Timber.d("## [카카오 로그아웃] 이미 로그아웃된 상태")
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            signOutKakaoUseCase()
                .onSuccess {
                    Timber.d("## [카카오 로그아웃] 로그아웃 성공")
                    _uiState.update {
                        it.copy(
                            user = null,
                            accessToken = null,
                            isSignIn = false,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e("## [카카오 로그아웃] 로그아웃 실패: ${e.message}")
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "카카오 로그아웃에 실패했습니다",
                            isLoading = false
                        )
                    }
                }
        } catch (e: Exception) {
            Timber.e("## [카카오 로그아웃] 로그아웃 에러: ${e.message}")
            _uiState.update {
                it.copy(
                    error = e.message ?: "카카오 로그아웃에 실패했습니다",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    data class KakaoLoginUiState(
        val isLoading: Boolean = false,
        val user: KakaoUserEntity? = null,
        val accessToken: String? = null,
        val isSignIn: Boolean = false,
        val error: String? = null
    )
}