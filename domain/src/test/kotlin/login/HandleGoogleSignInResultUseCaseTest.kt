package login

import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.usecase.login.google.HandleGoogleSignInResultUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class HandleGoogleSignInResultUseCaseTest {

    private val repository = mockk<GoogleAuthRepository>()
    private lateinit var useCase: HandleGoogleSignInResultUseCase

    private val mockIdToken = "mock_id_token"
    // domain 모듈에선 안드로이드 api를 쓸 수 없기 때문에 임의 프로퍼티 사용
    private val mockData = "mock_intent_data"

    @Before
    fun setup() {
        useCase = HandleGoogleSignInResultUseCase(repository)
    }

    @Test
    fun `구글 로그인 결과 처리 성공 테스트`() = runTest {
        // Given
        coEvery { repository.handleSignInResult(mockData) } returns Result.success(mockIdToken)

        // When
        val result = useCase(mockData)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockIdToken, result.getOrNull())

        coVerify { repository.handleSignInResult(mockData) }
    }

    @Test
    fun `구글 로그인 결과 처리 실패 테스트`() = runTest {
        // Given
        val error = Exception("로그인 결과 처리 실패")
        coEvery { repository.handleSignInResult(mockData) } returns Result.failure(error)

        // When
        val result = useCase(mockData)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error.message, result.exceptionOrNull()?.message)

        coVerify { repository.handleSignInResult(mockData) }
    }

    @Test
    fun `null 데이터로 구글 로그인 결과 처리 테스트`() = runTest {
        // Given
        val error = Exception("데이터가 없습니다")
        coEvery { repository.handleSignInResult(null) } returns Result.failure(error)

        // When
        val result = useCase(null)

        // Then
        assertTrue(result.isFailure)

        coVerify { repository.handleSignInResult(null) }
    }
}