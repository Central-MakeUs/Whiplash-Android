package com.whiplash.presentation.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.entity.UserEntity
import com.whiplash.domain.usecase.login.google.GetCurrentUserUseCase
import com.whiplash.domain.usecase.login.google.GetGoogleSignInIntentUseCase
import com.whiplash.domain.usecase.login.google.HandleGoogleSignInResultUseCase
import com.whiplash.domain.usecase.login.google.SignInWithGoogleUseCase
import com.whiplash.domain.usecase.login.google.SignOutUseCase
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
class GoogleLoginViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val getGoogleSignInIntentUseCase: GetGoogleSignInIntentUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val handleGoogleSignInResultUseCase: HandleGoogleSignInResultUseCase,
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // 이 뷰모델이 초기화될 때 현재 로그인 상태 확인
        // 이미 로그인한 상태라면 그에 맞는 처리 수행
        checkCurrentUser()
    }

    /**
     * 현재 로그인된 유저가 있는지 확인
     */
    private fun checkCurrentUser() {
        val currentUser = getCurrentUserUseCase()
        _uiState.update {
            it.copy(
                user = currentUser,
                isSignIn = (currentUser != null)
            )
        }
    }

    /**
     * 구글 로그인을 위한 인텐트를 가져온다
     *
     * 액티비티에서 런처를 통해 실행해 구글 로그인 시작
     *
     * @return 구글 로그인 인텐트
     */
    fun getGoogleSignInIntent(): Intent = getGoogleSignInIntentUseCase() as Intent

    /**
     * 구글 로그인 결과 처리
     *
     * @param data 구글 로그인 결과(idToken 등)를 가진 인텐트
     */
    fun handleSignInResult(data: Intent?) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _uiState.update { it.copy(isLoading = true) }

            handleGoogleSignInResultUseCase.invoke(data)
                .onSuccess { idToken ->
                    Timber.d("## [구글 로그인] 로그인 성공. idToken : $idToken")
                    signInWithGoogleToken(idToken)
                }
                .onFailure { e ->
                    Timber.e("## [구글 로그인] 로그인 결과 처리 실패: ${e.message}")
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "로그인에 실패했습니다",
                            isLoading = false
                        )
                    }
                }
        } catch (e: Exception) {
            Timber.e("## [구글 로그인] 로그인 결과 처리 에러: ${e.message}")
            _uiState.update {
                it.copy(
                    error = e.message ?: "로그인에 실패했습니다",
                    isLoading = false
                )
            }
        }
    }

    /**
     * idToken으로 파이어베이스 인증 진행
     *
     * @param idToken 구글 로그인 후 받은 idToken
     */
    private suspend fun signInWithGoogleToken(idToken: String) {
        try {
            signInWithGoogleUseCase.invoke(idToken)
                .onSuccess { user ->
                    Timber.d("## [구글 로그인] 파이어베이스 인증 성공. 유저 정보 : $user")
                    _uiState.update {
                        it.copy(
                            user = user,
                            isSignIn = true,
                            isLoading = false
                        )
                    }
                }.onFailure { e ->
                    Timber.e("## [구글 로그인] 파이어베이스 인증 실패 : ${e.message}")
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "로그인에 실패했습니다",
                            isLoading = false
                        )
                    }
                }
        } catch (e: Exception) {
            Timber.e("## [구글 로그인] 파이어베이스 인증 에러 : ${e.message}")
            _uiState.update {
                it.copy(
                    error = e.message ?: "로그인에 실패했습니다",
                    isLoading = false
                )
            }
        }
    }

    /**
     * 구글 로그아웃
     */
    fun signOut() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _uiState.update { it.copy(isLoading = true) }
            signOutUseCase.invoke()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            user = null,
                            isSignIn = false,
                            isLoading = false
                        )
                    }
                }.onFailure { e ->
                    Timber.e("## [구글 로그아웃] 구글 로그아웃 실패 : $e")
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "로그아웃에 실패했습니다",
                            isLoading = false
                        )
                    }
                }
        } catch (e: Exception) {
            Timber.e("## [구글 로그아웃] 구글 로그아웃 에러 : $e")
            _uiState.update {
                it.copy(
                    error = e.message ?: "로그아웃에 실패했습니다",
                    isLoading = false
                )
            }
        }
    }

    /**
     * 구글 로그인 에러 상태 초기화
     */
    fun clearError() = _uiState.update { it.copy(error = null) }

    data class LoginUiState(
        // 구글 로그인 진행 상태
        val isLoading: Boolean = false,

        // 구글 로그인 후 받는 유저 정보(idToken 제외)
        val user: UserEntity? = null,

        // 로그인 됐는가?
        val isSignIn: Boolean = false,

        // 구글 로그인, 로그아웃 에러
        val error: String? = null
    )
}