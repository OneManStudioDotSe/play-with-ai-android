package se.onemanstudio.playaroundwithai.core.auth.model

import java.time.Instant

data class AuthSession(
    val userId: String,
    val isAnonymous: Boolean,
    val isNewUser: Boolean,
    val authProvider: String,
    val createdAt: Instant,
    val lastSignInAt: Instant,
    val sessionDuration: Long,
    val accountAgeDays: Long
)
