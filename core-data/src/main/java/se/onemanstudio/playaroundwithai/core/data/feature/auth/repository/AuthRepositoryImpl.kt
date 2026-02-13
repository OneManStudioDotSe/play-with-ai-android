package se.onemanstudio.playaroundwithai.core.data.feature.auth.repository

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import se.onemanstudio.playaroundwithai.core.data.feature.auth.mapper.toDomain
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.model.AuthSession
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository.AuthRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    override suspend fun signInAnonymously(): Result<AuthSession> {
        return try {
            val session = if (!isUserSignedIn()) {
                Timber.d("Auth - User not signed in, attempting anonymous sign-in...")
                val authResult = firebaseAuth.signInAnonymously().await()
                Timber.d("Auth - Anonymous sign-in successful. UID: ${authResult.user?.uid}")
                authResult.toDomain()
            } else {
                Timber.d("Auth - User already signed in. UID: ${firebaseAuth.currentUser?.uid}")
                requireNotNull(firebaseAuth.currentUser).toDomain()
            }

            Timber.d(
                "Auth - Session created: userId=%s, isNew=%s, accountAge=%d days, provider=%s",
                session.userId, session.isNewUser, session.accountAgeDays, session.authProvider
            )
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
        Timber.v("Auth - Auth check: isUserSignedIn=$signedIn")
        return signedIn
    }
}
