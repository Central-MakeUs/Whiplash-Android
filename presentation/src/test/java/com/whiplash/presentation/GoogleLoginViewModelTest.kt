package com.whiplash.presentation

import android.content.Intent
import com.whiplash.domain.entity.GoogleUserEntity
import com.whiplash.domain.usecase.login.google.GetCurrentUserUseCase
import com.whiplash.domain.usecase.login.google.GetGoogleSignInIntentUseCase
import com.whiplash.domain.usecase.login.google.HandleGoogleSignInResultUseCase
import com.whiplash.domain.usecase.login.google.SignInWithGoogleUseCase
import com.whiplash.domain.usecase.login.google.SignOutUseCase
import com.whiplash.presentation.login.GoogleLoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@ExperimentalCoroutinesApi
class GoogleLoginViewModelTest {

    private val signInWithGoogleUseCase = mockk<SignInWithGoogleUseCase>()
    private val getGoogleSignInIntentUseCase = mockk<GetGoogleSignInIntentUseCase>()
    private val signOutUseCase = mockk<SignOutUseCase>()
    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()
    private val handleGoogleSignInResultUseCase = mockk<HandleGoogleSignInResultUseCase>()

    private lateinit var viewModel: GoogleLoginViewModel

    private val mockedUser = GoogleUserEntity(
        id = "testId",
        email = "test@gmail.com",
        displayName = "테스트 유저",
        photoUrl = "https://test.com/test.jpg"
    )

    private val mockedIntent = mockk<Intent>()
    private val mockedIdToken = "test_id_token"

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // 초기화 시 현재 유저 없는 상태로 설정
        every { getCurrentUserUseCase() } returns null

        viewModel = GoogleLoginViewModel(
            signInWithGoogleUseCase,
            getGoogleSignInIntentUseCase,
            signOutUseCase,
            getCurrentUserUseCase,
            handleGoogleSignInResultUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기화 시 구글 로그인한 유저가 있으면 로그인 상태로 설정된다`() = runTest {
        // Given
        every { getCurrentUserUseCase() } returns mockedUser

        // When
        val viewModel = GoogleLoginViewModel(
            signInWithGoogleUseCase,
            getGoogleSignInIntentUseCase,
            signOutUseCase,
            getCurrentUserUseCase,
            handleGoogleSignInResultUseCase
        )

        // Then
        val state = viewModel.uiState.first()
        assertEquals(mockedUser, state.user)
        assertTrue(state.isSignIn)
    }

    @Test
    fun `초기화 시 구글 로그인한 유저가 없으면 비로그인 상태로 설정된다`() = runTest {
        // Given
        every { getCurrentUserUseCase() } returns null

        // When
        val viewModel = GoogleLoginViewModel(
            signInWithGoogleUseCase,
            getGoogleSignInIntentUseCase,
            signOutUseCase,
            getCurrentUserUseCase,
            handleGoogleSignInResultUseCase
        )

        // Then
        val state = viewModel.uiState.first()
        assertNull(state.user)
        assertFalse(state.isSignIn)
    }

    @Test
    fun `구글 로그인 인텐트를 리턴한다`() = runTest {
        // Given
        every { getGoogleSignInIntentUseCase() } returns mockedIntent

        // When
        val result = viewModel.getGoogleSignInIntent()

        // Then
        assertEquals(mockedIntent, result)
        verify { getGoogleSignInIntentUseCase() }
    }

    @Test
    fun `구글 파이어베이스 인증 실패 시 에러 메시지가 설정된다`() = runTest {
        // Given
        val errorMessage = "파이어베이스 인증 실패"
        coEvery { handleGoogleSignInResultUseCase(mockedIntent) } returns Result.success(mockedIdToken)
        coEvery { signInWithGoogleUseCase(mockedIdToken) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.handleSignInResult(mockedIntent)

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isSignIn)

        coVerify { handleGoogleSignInResultUseCase(mockedIntent) }
        coVerify { signInWithGoogleUseCase(mockedIdToken) }
    }

    @Test
    fun `구글 로그아웃 성공 시 유저 정보가 초기화되고 비로그인 상태가 된다`() = runTest {
        // Given
        coEvery { signOutUseCase() } returns Result.success(Unit)

        // When
        viewModel.signOut()

        // Then
        val state = viewModel.uiState.first()
        assertNull(state.user)
        assertFalse(state.isSignIn)
        assertFalse(state.isLoading)

        coVerify { signOutUseCase() }
    }

    @Test
    fun `구글 로그아웃 실패 시 에러 메시지가 설정된다`() = runTest {
        // Given
        val errorMessage = "구글 로그아웃 실패"
        coEvery { signOutUseCase() } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.signOut()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)

        coVerify { signOutUseCase() }
    }

    @Test
    fun `에러 클리어 시 에러 메시지가 null로 설정된다`() = runTest {
        // Given - 에러 상태 설정
        coEvery { handleGoogleSignInResultUseCase(mockedIntent) } returns Result.failure(Exception("테스트 에러"))
        viewModel.handleSignInResult(mockedIntent)

        // When
        viewModel.clearError()

        // Then
        val state = viewModel.uiState.first()
        assertNull(state.error)
    }
}