package se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.PromptFirestoreDto
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val LOG_PREVIEW_LENGTH = 50

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun savePrompt(text: String, timestamp: Long): Result<String> {
        val userId = auth.currentUser?.uid ?: "anonymous"
        val userPromptsCollection = firestore.collection("users").document(userId).collection("prompts")

        Timber.d("Firestore - Saving prompt at 'users/$userId/prompts' with text: '${text.take(LOG_PREVIEW_LENGTH)}...'")

        val dto = PromptFirestoreDto(
            text = text,
            timestamp = timestamp
        )

        return try {
            val docRef = userPromptsCollection.add(dto).await()
            Timber.d("Firestore - Prompt saved at users/$userId/prompts/${docRef.id}")
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseException) {
            Timber.e(e, "Firestore - Failed to save prompt")
            Result.failure(e)
        }
    }

    suspend fun updatePrompt(docId: String, text: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: "anonymous"
        val docRef = firestore.collection("users").document(userId).collection("prompts").document(docId)

        Timber.d("Firestore - Updating prompt at 'users/$userId/prompts/$docId' with text: '${text.take(LOG_PREVIEW_LENGTH)}...'")

        return try {
            docRef.update("text", text).await()
            Timber.d("Firestore - Prompt updated at users/$userId/prompts/$docId")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseException) {
            Timber.e(e, "Firestore - Failed to update prompt $docId")
            Result.failure(e)
        }
    }
}
