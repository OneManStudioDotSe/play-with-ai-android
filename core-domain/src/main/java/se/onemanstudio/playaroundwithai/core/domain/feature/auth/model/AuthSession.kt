package se.onemanstudio.playaroundwithai.core.domain.feature.auth.model

import java.util.Date

data class AuthSession(
    val userId: String,
    val isAnonymous: Boolean,
    val isNewUser: Boolean,
    val authProvider: String,
    val createdAt: Date,
    val lastSignInAt: Date,
    val sessionDuration: Long,
    val accountAgeDays: Long
)
