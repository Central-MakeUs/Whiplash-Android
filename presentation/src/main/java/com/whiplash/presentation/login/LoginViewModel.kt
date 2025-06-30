package com.whiplash.presentation.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whiplash.domain.model.UserEntity
import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.usecase.login.GetCurrentUserUseCase
import com.whiplash.domain.usecase.login.GetGoogleSignInIntentUseCase
import com.whiplash.domain.usecase.login.HandleGoogleSignInResultUseCase
import com.whiplash.domain.usecase.login.SignInWithGoogleUseCase
import com.whiplash.domain.usecase.login.SignOutUseCase
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
class LoginViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val getGoogleSignInIntentUseCase: GetGoogleSignInIntentUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val handleGoogleSignInResultUseCase: HandleGoogleSignInResultUseCase,
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = getCurrentUserUseCase()
        _uiState.update {
            it.copy(
                user = currentUser,
                isSignIn = (currentUser != null)
            )
        }
    }

    fun getGoogleSignInIntent(): Intent = getGoogleSignInIntentUseCase() as Intent

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

    fun clearError() = _uiState.update { it.copy(error = null) }

    data class LoginUiState(
        val isLoading: Boolean = false,
        val user: UserEntity? = null,
        val isSignIn: Boolean = false,
        val error: String? = null
    )
}