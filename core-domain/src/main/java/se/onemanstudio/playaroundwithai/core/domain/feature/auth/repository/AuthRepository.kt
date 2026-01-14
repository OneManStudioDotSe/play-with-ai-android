package se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository

interface AuthRepository {
    suspend fun signInAnonymously(): Result<Unit>
    fun isUserSignedIn(): Boolean
}
