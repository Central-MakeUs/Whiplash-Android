package login.kakao

import com.whiplash.domain.entity.auth.response.KakaoUserEntity
import com.whiplash.domain.repository.login.KakaoAuthRepository
import com.whiplash.domain.usecase.login.kakao.SignInWithKakaoUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SignInWithKakaoUseCaseTest {

    private val repository = mockk<KakaoAuthRepository>()
    private lateinit var useCase: SignInWithKakaoUseCase

    private val mockedUser = KakaoUserEntity(
        id = "testId",
        email = "test@test.com",
        nickname = "테스트 유저",
        profileImageUrl = "https://test.com/test.jpg"
    )

    private val mockedAccessToken = "test_access_token"

    @Before
    fun setup() {
        useCase = SignInWithKakaoUseCase(repository)
    }

    @Test
    fun `accessToken으로 카카오 로그인 성공 테스트`() = runTest {
        // Given
        coEvery { repository.getUserInfoWithToken(mockedAccessToken) } returns Result.success(mockedUser)

        // When
        val result = useCase(mockedAccessToken)

        // Then
        assertTrue(result.isSuccess)
        val loginResult = result.getOrNull()
        assertEquals(mockedAccessToken, loginResult?.accessToken)
        assertEquals(mockedUser, loginResult?.user)

        coVerify { repository.getUserInfoWithToken(mockedAccessToken) }
    }

    @Test
    fun `accessToken으로 카카오 로그인 실패 테스트`() = runTest {
        // Given
        val error = Exception("카카오 로그인 실패")
        coEvery { repository.getUserInfoWithToken(mockedAccessToken) } returns Result.failure(error)

        // When
        val result = useCase(mockedAccessToken)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error.message, result.exceptionOrNull()?.message)

        coVerify { repository.getUserInfoWithToken(mockedAccessToken) }
    }
}