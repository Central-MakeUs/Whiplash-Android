package com.whiplash.presentation.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.auth.request.LoginRequestEntity
import com.whiplash.domain.usecase.auth.SocialLoginUseCase
import com.whiplash.domain.usecase.auth.SocialLogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleLoginManager: GoogleLoginManager,
    private val kakaoLoginManager: KakaoLoginManager,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val socialLogoutUseCase: SocialLogoutUseCase,
) : ViewModel() {

    data class LoginUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isLoginSuccess: Boolean = false,
        val accessToken: String? = null,
        val refreshToken: String? = null
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun getGoogleSignInIntent(): Intent = googleLoginManager.getGoogleSignInIntent()

    fun handleGoogleSignIn(data: Intent?, deviceId: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        try {
            googleLoginManager.handleGoogleSignIn(data)
                .fold(
                    onSuccess = { result ->
                        loginWithServer("GOOGLE", result.idToken, deviceId)
                    },
                    onFailure = { e ->
                        Timber.e("## 구글 로그인 실패: ${e.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message
                            )
                        }
                    }
                )
        } catch (e: Exception) {
            Timber.e("## 구글 로그인 에러: ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun handleKakaoSignIn(context: Context, deviceId: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        try {
            kakaoLoginManager.signIn(context)
                .fold(
                    onSuccess = { result ->
                        loginWithServer("KAKAO", result.accessToken, deviceId)
                    },
                    onFailure = { e ->
                        Timber.e("## 카카오 로그인 실패: ${e.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message
                            )
                        }
                    }
                )
        } catch (e: Exception) {
            Timber.e("## 카카오 로그인 에러: ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private suspend fun loginWithServer(socialType: String, token: String, deviceId: String) {
        try {
            val request = LoginRequestEntity(
                socialType = socialType,
                token = token,
                deviceId = deviceId
            )
            socialLoginUseCase(request).collect { result ->
                result.onSuccess { loginResponse ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true,
                            accessToken = loginResponse.accessToken,
                            refreshToken = loginResponse.refreshToken,
                            errorMessage = null
                        )
                    }
                }.onFailure { e ->
                    Timber.e("## 서버 로그인 실패: ${e.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("## 서버 로그인 에러: ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun signOut() = viewModelScope.launch {
        try {
            googleLoginManager.signOut()
            kakaoLoginManager.signOut()
            _uiState.update {
                LoginUiState()
            }
        } catch (e: Exception) {
            Timber.e("## 로그아웃 에러: ${e.message}")
        }
    }
}