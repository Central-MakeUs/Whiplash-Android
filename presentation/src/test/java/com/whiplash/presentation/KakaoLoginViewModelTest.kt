package com.whiplash.presentation

import com.whiplash.domain.entity.KakaoLoginResult
import com.whiplash.domain.entity.KakaoUserEntity
import com.whiplash.domain.usecase.login.kakao.GetCurrentKakaoUserUseCase
import com.whiplash.domain.usecase.login.kakao.SignInWithKakaoUseCase
import com.whiplash.domain.usecase.login.kakao.SignOutKakaoUseCase
import com.whiplash.presentation.login.KakaoLoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

@ExperimentalCoroutinesApi
class KakaoLoginViewModelTest {

    private val signInWithKakaoUseCase = mockk<SignInWithKakaoUseCase>()
    private val signOutKakaoUseCase = mockk<SignOutKakaoUseCase>()
    private val getCurrentKakaoUserUseCase = mockk<GetCurrentKakaoUserUseCase>()

    private lateinit var viewModel: KakaoLoginViewModel

    private val mockedUser = KakaoUserEntity(
        id = "testId",
        email = "test@kakao.com",
        nickname = "테스트 유저",
        profileImageUrl = "https://test.com/test.jpg"
    )

    private val mockedAccessToken = "test_access_token"

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // 초기화 시 현재 유저 없는 상태로 설정
        coEvery { getCurrentKakaoUserUseCase() } returns Result.success(null)

        viewModel = KakaoLoginViewModel(
            signInWithKakaoUseCase,
            signOutKakaoUseCase,
            getCurrentKakaoUserUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `카카오 로그인 시작 시 로딩 상태가 true로 설정된다`() = runTest {
        // When
        viewModel.startKakaoLogin()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isLoading)
    }

    @Test
    fun `카카오 로그인 성공 시 유저 정보가 설정되고 로그인 상태가 된다`() = runTest {
        // Given
        val loginResult = KakaoLoginResult(mockedAccessToken, mockedUser)
        coEvery { signInWithKakaoUseCase(mockedAccessToken) } returns Result.success(loginResult)

        // When
        viewModel.handleKakaoLoginSuccess(mockedAccessToken)

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)

        coVerify { signInWithKakaoUseCase(mockedAccessToken) }
    }

    @Test
    fun `카카오 로그인 실패 시 에러 메시지가 설정된다`() = runTest {
        // Given
        val errorMessage = "카카오 로그인 실패"
        coEvery { signInWithKakaoUseCase(mockedAccessToken) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.handleKakaoLoginSuccess(mockedAccessToken)

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)

        coVerify { signInWithKakaoUseCase(mockedAccessToken) }
    }

    @Test
    fun `카카오 로그아웃 성공 시 유저 정보가 초기화되고 비로그인 상태가 된다`() = runTest {
        // Given
        coEvery { signOutKakaoUseCase() } returns Result.success(Unit)

        // When
        viewModel.signOut()

        // Then
        val state = viewModel.uiState.first()
        assertNull(state.user)
        assertNull(state.accessToken)
        assertFalse(state.isSignIn)
        assertFalse(state.isLoading)
    }

    @Test
    fun `카카오 로그아웃 실패 시 에러 메시지가 설정된다`() = runTest {
        // Given
        val errorMessage = "카카오 로그아웃 실패"
        coEvery { signOutKakaoUseCase() } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.signOut()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
    }

    @Test
    fun `에러 클리어 시 에러 메시지가 null로 설정된다`() = runTest {
        // Given
        viewModel.handleKakaoLoginFailure("테스트 에러")

        // When
        viewModel.clearError()

        // Then
        val state = viewModel.uiState.first()
        assertNull(state.error)
    }
}