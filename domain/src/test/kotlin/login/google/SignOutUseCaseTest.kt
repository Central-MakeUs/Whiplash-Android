package login.google

import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.usecase.login.google.SignOutUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SignOutUseCaseTest {

    private val repository = mockk<GoogleAuthRepository>()
    private lateinit var useCase: SignOutUseCase

    @Before
    fun setup() {
        useCase = SignOutUseCase(repository)
    }

    @Test
    fun `로그아웃 성공 테스트`() = runTest {
        // Given
        coEvery { repository.signOut() } returns Result.success(Unit)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)

        coVerify { repository.signOut() }
    }

    @Test
    fun `로그아웃 실패 테스트`() = runTest {
        // Given
        val error = Exception("로그아웃 실패")
        coEvery { repository.signOut() } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error.message, result.exceptionOrNull()?.message)

        coVerify { repository.signOut() }
    }
}
