package se.onemanstudio.playaroundwithai.data.remote.gemini

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    @SerializedName("inline_data")
    val inlineData: ImageData? = null
)

data class ImageData(
    @SerializedName("mime_type")
    val mimeType: String,
    val data: String // Base64 encoded image string
)

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
