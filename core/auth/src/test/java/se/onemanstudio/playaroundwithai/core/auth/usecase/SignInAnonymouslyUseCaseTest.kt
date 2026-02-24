package se.onemanstudio.playaroundwithai.core.auth.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.auth.model.AuthSession
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import java.time.Instant

class SignInAnonymouslyUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignInAnonymouslyUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = SignInAnonymouslyUseCase(authRepository)
    }

    @Test
    fun `invoke when repository succeeds returns AuthSession`() = runTest {
        // GIVEN: Repository returns a successful AuthSession
        val expectedSession = AuthSession(
            userId = "test-uid",
            isAnonymous = true,
            isNewUser = true,
            authProvider = "firebase",
            createdAt = Instant.ofEpochMilli(1_700_000_000_000L),
            lastSignInAt = Instant.ofEpochMilli(1_700_000_060_000L),
            sessionDuration = 60_000L,
            accountAgeDays = 10
        )
        coEvery { authRepository.signInAnonymously() } returns Result.success(expectedSession)

        // WHEN
        val result = useCase()

        // THEN
        assertThat(result.isSuccess).isTrue()
        val session = result.getOrThrow()
        assertThat(session.userId).isEqualTo("test-uid")
        assertThat(session.isAnonymous).isTrue()
        assertThat(session.isNewUser).isTrue()
        assertThat(session.authProvider).isEqualTo("firebase")
        assertThat(session.sessionDuration).isEqualTo(60_000L)
        assertThat(session.accountAgeDays).isEqualTo(10)
    }

    @Test
    fun `invoke when repository fails returns failure`() = runTest {
        // GIVEN: Repository returns a failure
        val expectedException = RuntimeException("Auth failed")
        coEvery { authRepository.signInAnonymously() } returns Result.failure(expectedException)

        // WHEN
        val result = useCase()

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(expectedException)
    }
}
