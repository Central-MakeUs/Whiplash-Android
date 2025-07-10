package com.whiplash.presentation.login

import android.content.Intent
import com.whiplash.domain.entity.GoogleUserEntity
import com.whiplash.domain.usecase.login.google.GetGoogleSignInIntentUseCase
import com.whiplash.domain.usecase.login.google.HandleGoogleSignInResultUseCase
import com.whiplash.domain.usecase.login.google.SignInWithGoogleUseCase
import com.whiplash.domain.usecase.login.google.SignOutUseCase
import com.whiplash.domain.usecase.login.google.GetCurrentUserUseCase
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleLoginManager @Inject constructor(
    private val getGoogleSignInIntentUseCase: GetGoogleSignInIntentUseCase,
    private val handleGoogleSignInResultUseCase: HandleGoogleSignInResultUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {

    /**
     * 구글 로그인 Intent 가져오기
     */
    fun getGoogleSignInIntent(): Intent = getGoogleSignInIntentUseCase() as Intent

    /**
     * 현재 로그인된 사용자 정보 가져오기
     */
    fun getCurrentUser(): GoogleUserEntity? = getCurrentUserUseCase()

    /**
     * 구글 로그인 전체 프로세스 처리 (Intent 결과 → Firebase 로그인)
     */
    suspend fun handleGoogleSignIn(data: Intent?): Result<GoogleUserEntity> {
        return try {
            handleGoogleSignInResultUseCase.invoke(data)
                .fold(
                    onSuccess = { idToken ->
                        Timber.d("## [GoogleLoginManager] 구글 로그인 성공. idToken: $idToken")
                        signInWithGoogleUseCase.invoke(idToken)
                            .fold(
                                onSuccess = { user ->
                                    Timber.d("## [GoogleLoginManager] Firebase 인증 성공. 사용자: $user")
                                    Result.success(user)
                                },
                                onFailure = { e ->
                                    Timber.e("## [GoogleLoginManager] Firebase 인증 실패: ${e.message}")
                                    Result.failure(e)
                                }
                            )
                    },
                    onFailure = { e ->
                        Timber.e("## [GoogleLoginManager] 구글 로그인 결과 처리 실패: ${e.message}")
                        Result.failure(e)
                    }
                )
        } catch (e: Exception) {
            Timber.e("## [GoogleLoginManager] 구글 로그인 에러: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 구글 로그아웃
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            signOutUseCase.invoke()
                .also { result ->
                    if (result.isSuccess) {
                        Timber.d("## [GoogleLoginManager] 구글 로그아웃 성공")
                    } else {
                        Timber.e("## [GoogleLoginManager] 구글 로그아웃 실패: ${result.exceptionOrNull()?.message}")
                    }
                }
        } catch (e: Exception) {
            Timber.e("## [GoogleLoginManager] 구글 로그아웃 에러: ${e.message}")
            Result.failure(e)
        }
    }
}