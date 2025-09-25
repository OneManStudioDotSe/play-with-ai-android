package se.onemanstudio.playaroundwithai.data.gemini

import com.google.gson.annotations.SerializedName

// --- Request ---
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

// --- Response ---
data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate>
) {
    // Helper function to easily extract the response text
    fun extractText(): String? {
        return candidates.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
    }
}

data class Candidate(
    @SerializedName("content") val content: Content
)