package se.onemanstudio.playaroundwithai.data.remote.gemini

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.BuildConfig
import se.onemanstudio.playaroundwithai.data.remote.gemini.network.GeminiApiService
import se.onemanstudio.playaroundwithai.data.local.PromptDao
import se.onemanstudio.playaroundwithai.data.local.PromptEntity
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.graphics.scale

// 1. Define your fixed context here
private const val SYSTEM_INSTRUCTION = """
    You are a fun and slightly sarcastic AI assistant named 'Chip'.
    Your goal is to provide helpful, but witty and concise answers.
    Always keep your response under 50 words and never break character.
"""

@Singleton
class GeminiRepository @Inject constructor(
    private val apiService: GeminiApiService,
    private val promptDao: PromptDao // Inject the DAO
) {
    suspend fun generateContent(prompt: String, image: Bitmap?): Result<GeminiResponse> {
        return try {
            val parts = mutableListOf<Part>()

            // 2. Combine the system instruction with the user's prompt
            val fullPrompt = "$SYSTEM_INSTRUCTION\n\nUser prompt: $prompt"
            parts.add(Part(text = fullPrompt))

            // Add the image part if an image is provided
            image?.let { parts.add(Part(inlineData = it.toImageData())) }

            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper function to process the image
    private fun Bitmap.toImageData(): ImageData {
        // 1. Scale the bitmap
        val scaledBitmap = this.scaleBitmap(768) // Max dimension of 768px

        // 2. Compress the scaled bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream) // 75% quality
        val byteArray = byteArrayOutputStream.toByteArray()

        // 3. Encode with NO_WRAP flag
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP) // <-- THE FIX

        return ImageData(mimeType = "image/jpeg", data = base64String)
    }

    // New helper function to resize the bitmap while maintaining aspect ratio
    private fun Bitmap.scaleBitmap(maxDimension: Int): Bitmap {
        val originalWidth = this.width
        val originalHeight = this.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension

        if (originalHeight > originalWidth) {
            resizedWidth = (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        } else if (originalWidth > originalHeight) {
            resizedHeight = (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        }

        return this.scale(resizedWidth, resizedHeight, false)
    }

    // New function to save a prompt
    suspend fun savePrompt(promptText: String) {
        promptDao.insertPrompt(PromptEntity(text = promptText))
    }

    // New function to get the history
    fun getPromptHistory(): Flow<List<PromptEntity>> = promptDao.getPromptHistory()
}
