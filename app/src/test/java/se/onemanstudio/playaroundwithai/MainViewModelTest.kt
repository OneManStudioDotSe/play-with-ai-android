package se.onemanstudio.playaroundwithai

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.auth.model.AuthSession
import se.onemanstudio.playaroundwithai.core.auth.usecase.SignInAnonymouslyUseCase
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageEvent
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.util.MainCoroutineRule
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private val fakeUsageFlow = MutableSharedFlow<TokenUsageEvent>()

    private val tokenUsageTracker = mockk<TokenUsageTracker>(relaxed = true) {
        coEvery { lastUsageEvent } returns fakeUsageFlow
    }

    private fun createAuthSession(): AuthSession = AuthSession(
        userId = "test-uid",
        isAnonymous = true,
        isNewUser = true,
        authProvider = "firebase",
        createdAt = Instant.ofEpochMilli(1_700_000_000_000L),
        lastSignInAt = Instant.ofEpochMilli(1_700_000_060_000L),
        sessionDuration = 60_000L,
        accountAgeDays = 10
    )

    private fun createViewModel(
        signInResult: Result<AuthSession> = Result.success(createAuthSession()),
    ): MainViewModel {
        val signInAnonymouslyUseCase = mockk<SignInAnonymouslyUseCase> {
            coEvery { this@mockk.invoke() } returns signInResult
        }

        return MainViewModel(signInAnonymouslyUseCase, tokenUsageTracker)
    }

    // --- Test 1: init calls signIn and on success sets authError = false ---

    @Test
    fun `init signs in successfully and authError is false`() = runTest {
        // Given & When
        val viewModel = createViewModel(signInResult = Result.success(createAuthSession()))
        advanceUntilIdle()

        // Then
        assertThat(viewModel.authError.value).isFalse()
        assertThat(viewModel.authRetriesExhausted.value).isFalse()
    }

    // --- Test 2: init calls signIn and on failure sets authError = true ---

    @Test
    fun `init signs in with failure and authError is true`() = runTest {
        // Given & When
        val viewModel = createViewModel(signInResult = Result.failure(RuntimeException("Auth failed")))
        advanceUntilIdle()

        // Then
        assertThat(viewModel.authError.value).isTrue()
    }

    // --- Test 3: retryAuth when not exhausted retries signIn ---

    @Test
    fun `retryAuth when not exhausted calls signIn again and succeeds`() = runTest {
        // Given - first call fails, second call succeeds
        val signInAnonymouslyUseCase = mockk<SignInAnonymouslyUseCase>()
        coEvery { signInAnonymouslyUseCase.invoke() } returnsMany listOf(
            Result.failure(RuntimeException("Auth failed")),
            Result.success(createAuthSession())
        )
        val viewModel = MainViewModel(signInAnonymouslyUseCase, tokenUsageTracker)
        advanceUntilIdle()

        // Verify initial failure
        assertThat(viewModel.authError.value).isTrue()

        // When
        viewModel.retryAuth()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.authError.value).isFalse()
        assertThat(viewModel.authRetriesExhausted.value).isFalse()
    }

    // --- Test 4: retryAuth when exhausted does nothing ---

    @Test
    fun `retryAuth when exhausted does nothing`() = runTest {
        // Given - exhaust all 3 attempts (1 init + 2 retries)
        val signInAnonymouslyUseCase = mockk<SignInAnonymouslyUseCase>()
        coEvery { signInAnonymouslyUseCase.invoke() } returns Result.failure(RuntimeException("Auth failed"))
        val viewModel = MainViewModel(signInAnonymouslyUseCase, tokenUsageTracker)
        advanceUntilIdle()

        // Retry twice to exhaust attempts (init=1, retry=2, retry=3)
        viewModel.retryAuth()
        advanceUntilIdle()
        viewModel.retryAuth()
        advanceUntilIdle()

        assertThat(viewModel.authRetriesExhausted.value).isTrue()

        // When - try to retry after exhaustion
        viewModel.retryAuth()
        advanceUntilIdle()

        // Then - still exhausted, no further sign-in call
        assertThat(viewModel.authRetriesExhausted.value).isTrue()
        assertThat(viewModel.authError.value).isTrue()
    }

    // --- Test 5: authRetriesExhausted is set to true after MAX_AUTH_ATTEMPTS (3) failures ---

    @Test
    fun `authRetriesExhausted is true after 3 consecutive failures`() = runTest {
        // Given
        val signInAnonymouslyUseCase = mockk<SignInAnonymouslyUseCase>()
        coEvery { signInAnonymouslyUseCase.invoke() } returns Result.failure(RuntimeException("Auth failed"))
        val viewModel = MainViewModel(signInAnonymouslyUseCase, tokenUsageTracker)
        advanceUntilIdle()

        // Attempt 1 (init): authRetriesExhausted should be false (1 < 3)
        assertThat(viewModel.authRetriesExhausted.value).isFalse()
        assertThat(viewModel.authError.value).isTrue()

        // Attempt 2 (retry): authRetriesExhausted should be false (2 < 3)
        viewModel.retryAuth()
        advanceUntilIdle()
        assertThat(viewModel.authRetriesExhausted.value).isFalse()

        // Attempt 3 (retry): authRetriesExhausted should be true (3 >= 3)
        viewModel.retryAuth()
        advanceUntilIdle()
        assertThat(viewModel.authRetriesExhausted.value).isTrue()
    }

    // --- Test 6: Successful auth resets authAttemptCount and authRetriesExhausted ---

    @Test
    fun `successful auth after failures resets authRetriesExhausted`() = runTest {
        // Given - fail twice then succeed
        val signInAnonymouslyUseCase = mockk<SignInAnonymouslyUseCase>()
        coEvery { signInAnonymouslyUseCase.invoke() } returnsMany listOf(
            Result.failure(RuntimeException("Auth failed")),
            Result.failure(RuntimeException("Auth failed")),
            Result.success(createAuthSession())
        )
        val viewModel = MainViewModel(signInAnonymouslyUseCase, tokenUsageTracker)
        advanceUntilIdle()

        // Attempt 1 (init) fails
        assertThat(viewModel.authError.value).isTrue()

        // Attempt 2 (retry) fails
        viewModel.retryAuth()
        advanceUntilIdle()
        assertThat(viewModel.authError.value).isTrue()

        // Attempt 3 (retry) succeeds
        viewModel.retryAuth()
        advanceUntilIdle()

        // Then - everything resets
        assertThat(viewModel.authError.value).isFalse()
        assertThat(viewModel.authRetriesExhausted.value).isFalse()
    }
}
