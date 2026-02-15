package se.onemanstudio.playaroundwithai.core.data.feature.auth.mapper

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.model.AuthSession
import java.time.Instant
import java.util.concurrent.TimeUnit

private const val DEFAULT_PROVIDER = "anonymous"

fun AuthResult.toDomain(): AuthSession {
    val firebaseUser = requireNotNull(user) { "AuthResult.user must not be null after successful sign-in" }
    val creationTimestamp = firebaseUser.metadata?.creationTimestamp ?: 0L
    val lastSignInTimestamp = firebaseUser.metadata?.lastSignInTimestamp ?: 0L
    val nowMillis = System.currentTimeMillis()

    return AuthSession(
        userId = firebaseUser.uid,
        isAnonymous = firebaseUser.isAnonymous,
        isNewUser = additionalUserInfo?.isNewUser ?: true,
        authProvider = additionalUserInfo?.providerId ?: DEFAULT_PROVIDER,
        createdAt = Instant.ofEpochMilli(creationTimestamp),
        lastSignInAt = Instant.ofEpochMilli(lastSignInTimestamp),
        sessionDuration = lastSignInTimestamp - creationTimestamp,
        accountAgeDays = TimeUnit.MILLISECONDS.toDays(nowMillis - creationTimestamp)
    )
}

fun FirebaseUser.toDomain(): AuthSession {
    val creationTimestamp = metadata?.creationTimestamp ?: 0L
    val lastSignInTimestamp = metadata?.lastSignInTimestamp ?: 0L
    val nowMillis = System.currentTimeMillis()

    return AuthSession(
        userId = uid,
        isAnonymous = isAnonymous,
        isNewUser = false,
        authProvider = providerId,
        createdAt = Instant.ofEpochMilli(creationTimestamp),
        lastSignInAt = Instant.ofEpochMilli(lastSignInTimestamp),
        sessionDuration = lastSignInTimestamp - creationTimestamp,
        accountAgeDays = TimeUnit.MILLISECONDS.toDays(nowMillis - creationTimestamp)
    )
}
