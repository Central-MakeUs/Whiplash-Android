package com.whiplash.presentation.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.auth.request.LoginRequestEntity
import com.whiplash.domain.entity.auth.request.LogoutRequestEntity
import com.whiplash.domain.provider.CrashlyticsProvider
import com.whiplash.domain.provider.TokenProvider
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

// TODO : api 수정되면 MOCK 대신 GOOGLE, KAKAO 넘기게 수정
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleLoginManager: GoogleLoginManager,
    private val kakaoLoginManager: KakaoLoginManager,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val socialLogoutUseCase: SocialLogoutUseCase,
    private val tokenProvider: TokenProvider,
    private val crashlyticsProvider: CrashlyticsProvider,
) : ViewModel() {

    data class LoginUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isLoginSuccess: Boolean = false,
        val isLogoutSuccess: Boolean = false,
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun getGoogleSignInIntent(): Intent = googleLoginManager.getGoogleSignInIntent()

    fun handleGoogleSignIn(data: Intent?, deviceId: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        try {
            tokenProvider.saveDeviceId(deviceId)

            googleLoginManager.handleGoogleSignIn(data)
                .fold(
                    onSuccess = { result ->
                        invokeLogin("MOCK", result.idToken, deviceId)
                    },
                    onFailure = { e ->
                        crashlyticsProvider.recordError(e)
                        crashlyticsProvider.logError("구글 로그인 실패 : ${e.message}")
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
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("구글 로그인 에러 : ${e.message}")
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
            tokenProvider.saveDeviceId(deviceId)

            kakaoLoginManager.signIn(context)
                .fold(
                    onSuccess = { result ->
                        invokeLogin("MOCK", result.accessToken, deviceId)
                    },
                    onFailure = { e ->
                        Timber.e("## 카카오 로그인 실패: ${e.message}")
                        crashlyticsProvider.recordError(e)
                        crashlyticsProvider.logError("카카오 로그인 실패 : ${e.message}")
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
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("카카오 로그인 에러 : ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private suspend fun invokeLogin(socialType: String, token: String, deviceId: String) {
        try {
            val request = LoginRequestEntity(
                socialType = socialType,
                token = token,
                deviceId = deviceId
            )
            socialLoginUseCase(request).collect { result ->
                result.onSuccess { loginResponse ->
                    tokenProvider.saveTokens(
                        loginResponse.accessToken,
                        loginResponse.refreshToken
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true,
                            errorMessage = null
                        )
                    }
                }.onFailure { e ->
                    crashlyticsProvider.recordError(e)
                    crashlyticsProvider.logError("로그인 api 실패 : ${e.message}")
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
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("로그인 api 에러 : ${e.message}")
            Timber.e("## 서버 로그인 에러: ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun signOut(deviceId: String) = viewModelScope.launch {
        try {
            val logoutRequest = LogoutRequestEntity(deviceId = deviceId)
            socialLogoutUseCase(logoutRequest).collect { result ->
                result.onSuccess {
                    Timber.d("## [로그아웃] 성공")
                    googleLoginManager.signOut()
                    kakaoLoginManager.signOut()
                    tokenProvider.clearTokens()

                    _uiState.update {
                        it.copy(isLogoutSuccess = true)
                    }
                }.onFailure { e ->
                    crashlyticsProvider.recordError(e)
                    crashlyticsProvider.logError("로그아웃 api 실패 : ${e.message}")
                    Timber.e("## [로그아웃] 실패: ${e.message}")
                    googleLoginManager.signOut()
                    kakaoLoginManager.signOut()
                    tokenProvider.clearTokens()

                    _uiState.update {
                        it.copy(isLogoutSuccess = true)
                    }
                }
            }
        } catch (e: Exception) {
            crashlyticsProvider.recordError(e)
            crashlyticsProvider.logError("로그아웃 api 에러 : ${e.message}")
            Timber.e("## [로그아웃] 에러: ${e.message}")
            googleLoginManager.signOut()
            kakaoLoginManager.signOut()
            tokenProvider.clearTokens()

            _uiState.update {
                it.copy(isLogoutSuccess = true)
            }
        }
    }

    fun resetLogoutState() = _uiState.update { it.copy(isLogoutSuccess = false) }

}