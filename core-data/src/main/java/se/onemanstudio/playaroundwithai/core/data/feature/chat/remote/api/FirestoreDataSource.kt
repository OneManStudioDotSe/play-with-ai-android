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

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val promptsCollection = firestore.collection("prompts")

    suspend fun savePrompt(text: String, timestamp: Long): Result<Unit> {
        val userId = auth.currentUser?.uid ?: "anonymous"
        Timber.d("Firestore - Saving prompt at collection 'prompts' for user with id '$userId' the text: '${text.take(50)}...'")

        val dto = PromptFirestoreDto(
            text = text,
            timestamp = timestamp,
            userId = userId
        )

        return try {
            val docRef = promptsCollection.add(dto).await()
            Timber.d("Firestore - Prompt saved at documentId ${docRef.id}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseException) {
            Timber.e(e, "Firestore - Failed to save prompt")
            Result.failure(e)
        }
    }
}