package se.onemanstudio.playaroundwithai.core.auth.repository

import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        firebaseAuth = mockk()
        every { firebaseAuth.currentUser } returns null
        repository = AuthRepositoryImpl(firebaseAuth)

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `authReady starts false when no user is signed in`() {
        every { firebaseAuth.currentUser } returns null
        val repo = AuthRepositoryImpl(firebaseAuth)
        assertThat(repo.authReady.value).isFalse()
    }

    @Test
    fun `authReady starts true when user is already signed in`() {
        every { firebaseAuth.currentUser } returns mockk()
        val repo = AuthRepositoryImpl(firebaseAuth)
        assertThat(repo.authReady.value).isTrue()
    }

    @Test
    fun `signInAnonymously sets authReady to true on success`() = runTest {
        // GIVEN: User is not signed in
        val metadata = mockk<FirebaseUserMetadata> {
            every { creationTimestamp } returns 1_700_000_000_000L
            every { lastSignInTimestamp } returns 1_700_000_060_000L
        }
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "new-uid"
            every { isAnonymous } returns true
            every { this@mockk.metadata } returns metadata
        }
        val additionalUserInfo = mockk<AdditionalUserInfo> {
            every { isNewUser } returns true
            every { providerId } returns "firebase"
        }
        val authResult = mockk<AuthResult> {
            every { user } returns firebaseUser
            every { this@mockk.additionalUserInfo } returns additionalUserInfo
        }
        val task = mockk<Task<AuthResult>>()
        every { firebaseAuth.signInAnonymously() } returns task
        coEvery { task.await() } returns authResult

        assertThat(repository.authReady.value).isFalse()

        // WHEN
        repository.signInAnonymously()

        // THEN
        assertThat(repository.authReady.value).isTrue()
    }

    @Test
    fun `signInAnonymously when not signed in returns AuthSession with mapped fields`() = runTest {
        // GIVEN: User is not signed in and sign-in succeeds
        val metadata = mockk<FirebaseUserMetadata> {
            every { creationTimestamp } returns 1_700_000_000_000L
            every { lastSignInTimestamp } returns 1_700_000_060_000L
        }
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "new-uid"
            every { isAnonymous } returns true
            every { this@mockk.metadata } returns metadata
        }
        val additionalUserInfo = mockk<AdditionalUserInfo> {
            every { isNewUser } returns true
            every { providerId } returns "firebase"
        }
        val authResult = mockk<AuthResult> {
            every { user } returns firebaseUser
            every { this@mockk.additionalUserInfo } returns additionalUserInfo
        }
        val task = mockk<Task<AuthResult>>()
        every { firebaseAuth.signInAnonymously() } returns task
        coEvery { task.await() } returns authResult

        // WHEN
        val result = repository.signInAnonymously()

        // THEN
        assertThat(result.isSuccess).isTrue()
        val session = result.getOrThrow()
        assertThat(session.userId).isEqualTo("new-uid")
        assertThat(session.isAnonymous).isTrue()
        assertThat(session.isNewUser).isTrue()
        assertThat(session.authProvider).isEqualTo("firebase")
        assertThat(session.sessionDuration).isEqualTo(60_000L)
    }

    @Test
    fun `signInAnonymously when already signed in returns session from existing user`() = runTest {
        // GIVEN: User is already signed in
        val metadata = mockk<FirebaseUserMetadata> {
            every { creationTimestamp } returns 1_700_000_000_000L
            every { lastSignInTimestamp } returns 1_700_000_120_000L
        }
        val existingUser = mockk<FirebaseUser> {
            every { uid } returns "existing-uid"
            every { isAnonymous } returns true
            every { providerId } returns "firebase"
            every { this@mockk.metadata } returns metadata
        }
        every { firebaseAuth.currentUser } returns existingUser

        // WHEN
        val result = repository.signInAnonymously()

        // THEN
        assertThat(result.isSuccess).isTrue()
        val session = result.getOrThrow()
        assertThat(session.userId).isEqualTo("existing-uid")
        assertThat(session.isNewUser).isFalse()
        assertThat(session.sessionDuration).isEqualTo(120_000L)
    }

    @Test
    fun `signInAnonymously when Firebase throws returns failure`() = runTest {
        // GIVEN: User is not signed in and Firebase throws
        every { firebaseAuth.currentUser } returns null
        val firebaseException = mockk<FirebaseException>()
        every { firebaseAuth.signInAnonymously() } throws firebaseException

        // WHEN
        val result = repository.signInAnonymously()

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FirebaseException::class.java)
    }
}
