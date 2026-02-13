package se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository

import se.onemanstudio.playaroundwithai.core.domain.feature.auth.model.AuthSession

interface AuthRepository {
    suspend fun signInAnonymously(): Result<AuthSession>
    fun isUserSignedIn(): Boolean
}
