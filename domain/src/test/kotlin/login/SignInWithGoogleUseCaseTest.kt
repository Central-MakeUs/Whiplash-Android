package login

import com.whiplash.domain.model.UserEntity
import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.usecase.login.SignInWithGoogleUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SignInWithGoogleUseCaseTest {

    private val repository = mockk<GoogleAuthRepository>()
    private lateinit var useCase: SignInWithGoogleUseCase

    private val mockedUser = UserEntity(
        id = "testId",
        email = "test@test.com",
        displayName = "테스트 유저",
        photoUrl = "https://test.com/test.jpg"
    )

    private val mockedIdToken = "test_token"

    @Before
    fun setup() {
        useCase = SignInWithGoogleUseCase(repository)
    }

    @Test
    fun `idToken으로 구글 로그인 성공 테스트`() = runTest {
        // Given
        coEvery { repository.signInWithGoogleToken(mockedIdToken) } returns Result.success(mockedUser)

        // When
        val result = useCase(mockedIdToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockedUser, result.getOrNull())

        // signInWithGoogleToken()은 suspend fun이라 함수 호출 여부 확인 시 coVerify 사용
        coVerify { repository.signInWithGoogleToken(mockedIdToken) }
    }

    @Test
    fun `idToken으로 구글 로그인 실패 테스트`() = runTest {
        // Given
        val error = Exception("구글 로그인 실패")
        coEvery { repository.signInWithGoogleToken(mockedIdToken) } returns Result.failure(error)

        // When
        val result = useCase(mockedIdToken)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error.message, result.exceptionOrNull()?.message)

        // signInWithGoogleToken()은 suspend fun이라 함수 호출 여부 확인 시 coVerify 사용
        coVerify { repository.signInWithGoogleToken(mockedIdToken) }
    }

}