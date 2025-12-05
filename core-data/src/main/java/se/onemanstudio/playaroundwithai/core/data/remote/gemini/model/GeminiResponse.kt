package se.onemanstudio.playaroundwithai.core.data.remote.gemini.model

import com.google.gson.annotations.SerializedName

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
