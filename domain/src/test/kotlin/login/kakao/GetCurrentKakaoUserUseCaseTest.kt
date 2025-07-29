package login.kakao

import com.whiplash.domain.entity.auth.response.KakaoUserEntity
import com.whiplash.domain.repository.login.KakaoAuthRepository
import com.whiplash.domain.usecase.login.kakao.GetCurrentKakaoUserUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class GetCurrentKakaoUserUseCaseTest {

    private val repository = mockk<KakaoAuthRepository>()
    private lateinit var useCase: GetCurrentKakaoUserUseCase

    private val mockedUser = KakaoUserEntity(
        id = "testId",
        email = "test@kakao.com",
        nickname = "테스트 유저",
        profileImageUrl = "https://test.com/test.jpg"
    )

    @Before
    fun setup() {
        useCase = GetCurrentKakaoUserUseCase(repository)
    }

    @Test
    fun `현재 유저가 카카오 로그인한 상태면 그 유저의 정보를 리턴한다`() = runTest {
        // Given
        coEvery { repository.getCurrentKakaoUser() } returns Result.success(mockedUser)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockedUser, result.getOrNull())

        coVerify { repository.getCurrentKakaoUser() }
    }

    @Test
    fun `현재 유저가 카카오 로그인 되어있지 않다면 null을 리턴한다`() = runTest {
        // Given
        coEvery { repository.getCurrentKakaoUser() } returns Result.success(null)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())

        coVerify { repository.getCurrentKakaoUser() }
    }

    @Test
    fun `카카오 유저 정보 조회 실패 테스트`() = runTest {
        // Given
        val error = Exception("카카오 유저 정보 조회 실패")
        coEvery { repository.getCurrentKakaoUser() } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error.message, result.exceptionOrNull()?.message)

        coVerify { repository.getCurrentKakaoUser() }
    }
}