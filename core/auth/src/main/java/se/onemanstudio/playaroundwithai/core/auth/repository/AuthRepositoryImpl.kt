package se.onemanstudio.playaroundwithai.core.auth.repository

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import se.onemanstudio.playaroundwithai.core.auth.mapper.toDomain
import se.onemanstudio.playaroundwithai.core.auth.model.AuthSession
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    private val _authReady = MutableStateFlow(firebaseAuth.currentUser != null)
    override val authReady: StateFlow<Boolean> = _authReady.asStateFlow()

    override suspend fun signInAnonymously(): Result<AuthSession> {
        return try {
            val session = if (!isUserSignedIn()) {
                Timber.d("Auth - User not signed in, attempting anonymous sign-in...")
                val authResult = firebaseAuth.signInAnonymously().await()
                Timber.d("Auth - Anonymous sign-in successful and uid is ${authResult.user?.uid}")
                authResult.toDomain()
            } else {
                Timber.d("Auth - User already signed in and uid is ${firebaseAuth.currentUser?.uid}")
                requireNotNull(firebaseAuth.currentUser).toDomain()
            }

            Timber.d("Auth - Session created for ${session.userId} (isNew: ${session.isNewUser}, ${session.accountAgeDays} days old, from ${session.authProvider})")
            _authReady.value = true
            Result.success(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseException) {
            Timber.e(e, "Auth - Anonymous sign-in failed")
            Result.failure(e)
        }
    }

    override fun isUserSignedIn(): Boolean {
        val signedIn = firebaseAuth.currentUser != null
        Timber.d("Auth - Auth check: isUserSignedIn: $signedIn")
        return signedIn
    }

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid
}
