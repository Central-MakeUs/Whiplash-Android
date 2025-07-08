package login.google

import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.usecase.login.google.GetGoogleSignInIntentUseCase
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GetGoogleSignInIntentUseCaseTest {

    private val repository = mockk<GoogleAuthRepository>()
    private lateinit var useCase: GetGoogleSignInIntentUseCase

    // domain 모듈에선 안드로이드 api를 쓸 수 없기 때문에 임의 프로퍼티 사용
    private val mockIntent = "mock_intent_object"

    @Before
    fun setup() {
        useCase = GetGoogleSignInIntentUseCase(repository)
    }

    @Test
    fun `구글 로그인 인텐트를 반환한다`() {
        // Given
        every { repository.getGoogleSignInIntent() } returns mockIntent

        // When
        val result = useCase()

        // Then
        assertEquals(mockIntent, result)

        verify { repository.getGoogleSignInIntent() }
    }
}