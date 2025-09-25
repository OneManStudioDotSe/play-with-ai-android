package se.onemanstudio.playaroundwithai.data.remote.gemini

import com.google.gson.annotations.SerializedName

// --- Request ---
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

// Part can now contain either text or an image (inlineData)
data class Part(
    val text: String? = null,
    @SerializedName("inline_data")
    val inlineData: ImageData? = null
)

// New class to hold the image data
data class ImageData(
    @SerializedName("mime_type")
    val mimeType: String,
    val data: String // Base64 encoded image string
)


// --- Response ---
// The response structure can remain the same as it also uses the Content/Part model
data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate>
) {
    fun extractText(): String? {
        return candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }
}

data class Candidate(
    @SerializedName("content") val content: Content
)
