package se.onemanstudio.playaroundwithai.data.chat.data.remote

import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.data.chat.data.remote.dto.PromptFirestoreDto
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    suspend fun savePrompt(text: String, timestamp: Long): Result<String> {
        val userId = authRepository.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("User must be authenticated to sync prompts"))
        val userPromptsCollection = firestore.collection("users").document(userId).collection("prompts")

        val dto = PromptFirestoreDto(
            text = text,
            timestamp = timestamp
        )

        return try {
            val docRef = userPromptsCollection.add(dto).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseException) {
            Timber.e(e, "Firestore - Failed to save prompt")
            Result.failure(e)
        }
    }

    suspend fun updatePrompt(docId: String, text: String): Result<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("User must be authenticated to sync prompts"))
        val docRef = firestore.collection("users").document(userId).collection("prompts").document(docId)

        return try {
            docRef.update("text", text).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseException) {
            Timber.e(e, "Firestore - Failed to update prompt $docId")
            Result.failure(e)
        }
    }
}
