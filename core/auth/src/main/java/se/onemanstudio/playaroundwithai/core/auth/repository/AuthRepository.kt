package se.onemanstudio.playaroundwithai.core.auth.repository

import kotlinx.coroutines.flow.StateFlow
import se.onemanstudio.playaroundwithai.core.auth.model.AuthSession

interface AuthRepository {
    val authReady: StateFlow<Boolean>
    suspend fun signInAnonymously(): Result<AuthSession>
    fun isUserSignedIn(): Boolean
    fun getCurrentUserId(): String?
}
