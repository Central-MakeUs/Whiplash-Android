package login.google

import com.whiplash.domain.entity.GoogleUserEntity
import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.usecase.login.google.GetCurrentUserUseCase
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GetCurrentUserUseCaseTest {

    private val repository = mockk<GoogleAuthRepository>()
    private lateinit var useCase: GetCurrentUserUseCase

    private val mockedUser = GoogleUserEntity(
        id = "testId",
        email = "test@test.com",
        displayName = "테스트 유저",
        photoUrl = "https://test.com/test.jpg"
    )

    @Before
    fun setup() {
        useCase = GetCurrentUserUseCase(repository)
    }

    @Test
    fun `현재 유저가 로그인돼 있다면 그 유저 정보를 리턴한다`() {
        // Given
        every { repository.getCurrentUser() } returns mockedUser

        // When
        val result = useCase()

        // Then
        assertEquals(mockedUser, result)

        verify { repository.getCurrentUser() }
    }

    @Test
    fun `현재 유저가 로그인돼 있지 않거나 유저 정보가 없다면 null을 리턴한다`() {
        // Given
        every { repository.getCurrentUser() } returns null

        // When
        val result = useCase()

        // Then
        assertNull(result)

        verify { repository.getCurrentUser() }
    }
}