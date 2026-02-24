package se.onemanstudio.playaroundwithai.core.auth.mapper

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.concurrent.TimeUnit

class AuthMappersTest {

    @Test
    fun `AuthResult toDomain maps all fields correctly`() {
        // GIVEN: An AuthResult with known values
        val metadata = mockk<FirebaseUserMetadata> {
            every { creationTimestamp } returns 1_700_000_000_000L
            every { lastSignInTimestamp } returns 1_700_000_060_000L
        }
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "test-uid-123"
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

        // WHEN: We map AuthResult to domain
        val session = authResult.toDomain()

        // THEN: All fields are correctly mapped
        assertThat(session.userId).isEqualTo("test-uid-123")
        assertThat(session.isAnonymous).isTrue()
        assertThat(session.isNewUser).isTrue()
        assertThat(session.authProvider).isEqualTo("firebase")
        assertThat(session.createdAt.toEpochMilli()).isEqualTo(1_700_000_000_000L)
        assertThat(session.lastSignInAt.toEpochMilli()).isEqualTo(1_700_000_060_000L)
        assertThat(session.sessionDuration).isEqualTo(60_000L)
    }

    @Test
    fun `AuthResult toDomain computes accountAgeDays correctly`() {
        // GIVEN: An AuthResult with a creation timestamp 10 days ago
        val tenDaysAgoMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        val metadata = mockk<FirebaseUserMetadata> {
            every { creationTimestamp } returns tenDaysAgoMillis
            every { lastSignInTimestamp } returns System.currentTimeMillis()
        }
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "uid"
            every { isAnonymous } returns true
            every { this@mockk.metadata } returns metadata
        }
        val authResult = mockk<AuthResult> {
            every { user } returns firebaseUser
            every { additionalUserInfo } returns null
        }

        // WHEN
        val session = authResult.toDomain()

        // THEN
        assertThat(session.accountAgeDays).isEqualTo(10)
    }

    @Test
    fun `AuthResult toDomain uses defaults when additionalUserInfo is null`() {
        // GIVEN: An AuthResult with null additionalUserInfo
        val metadata = mockk<FirebaseUserMetadata> {
            every { creationTimestamp } returns 1_700_000_000_000L
            every { lastSignInTimestamp } returns 1_700_000_000_000L
        }
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "uid"
            every { isAnonymous } returns true
            every { this@mockk.metadata } returns metadata
        }
        val authResult = mockk<AuthResult> {
            every { user } returns firebaseUser
            every { additionalUserInfo } returns null
        }

        // WHEN
        val session = authResult.toDomain()

        // THEN: Defaults are applied
        assertThat(session.isNewUser).isTrue()
        assertThat(session.authProvider).isEqualTo("anonymous")
    }

    @Test
    fun `FirebaseUser toDomain maps fields and sets isNewUser to false`() {
        // GIVEN: A FirebaseUser that is already signed in
        val metadata = mockk<FirebaseUserMetadata> {
            every { creationTimestamp } returns 1_700_000_000_000L
            every { lastSignInTimestamp } returns 1_700_000_120_000L
        }
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "existing-uid"
            every { isAnonymous } returns true
            every { providerId } returns "firebase"
            every { this@mockk.metadata } returns metadata
        }

        // WHEN: We map an existing FirebaseUser to domain
        val session = firebaseUser.toDomain()

        // THEN: isNewUser is always false for existing users
        assertThat(session.userId).isEqualTo("existing-uid")
        assertThat(session.isNewUser).isFalse()
        assertThat(session.sessionDuration).isEqualTo(120_000L)
        assertThat(session.createdAt.toEpochMilli()).isEqualTo(1_700_000_000_000L)
        assertThat(session.lastSignInAt.toEpochMilli()).isEqualTo(1_700_000_120_000L)
    }

    @Test
    fun `FirebaseUser toDomain handles null metadata gracefully`() {
        // GIVEN: A FirebaseUser with null metadata
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "uid"
            every { isAnonymous } returns true
            every { providerId } returns "firebase"
            every { metadata } returns null
        }

        // WHEN
        val session = firebaseUser.toDomain()

        // THEN: Timestamps default to epoch
        assertThat(session.createdAt.toEpochMilli()).isEqualTo(0L)
        assertThat(session.lastSignInAt.toEpochMilli()).isEqualTo(0L)
        assertThat(session.sessionDuration).isEqualTo(0L)
    }
}
