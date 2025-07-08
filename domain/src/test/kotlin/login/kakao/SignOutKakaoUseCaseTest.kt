package login.kakao

import com.whiplash.domain.repository.login.KakaoAuthRepository
import com.whiplash.domain.usecase.login.kakao.SignOutKakaoUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SignOutKakaoUseCaseTest {

    private val repository = mockk<KakaoAuthRepository>()
    private lateinit var useCase: SignOutKakaoUseCase

    @Before
    fun setup() {
        useCase = SignOutKakaoUseCase(repository)
    }

    @Test
    fun `카카오 로그아웃 성공 테스트`() = runTest {
        // Given
        coEvery { repository.signOutKakao() } returns Result.success(Unit)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)

        coVerify { repository.signOutKakao() }
    }

    @Test
    fun `카카오 로그아웃 실패 테스트`() = runTest {
        // Given
        val error = Exception("카카오 로그아웃 실패")
        coEvery { repository.signOutKakao() } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error.message, result.exceptionOrNull()?.message)

        coVerify { repository.signOutKakao() }
    }
}