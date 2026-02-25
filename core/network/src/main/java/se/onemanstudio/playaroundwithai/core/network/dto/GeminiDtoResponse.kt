package se.onemanstudio.playaroundwithai.core.network.dto

import com.google.gson.annotations.SerializedName

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate>,
    @SerializedName("usageMetadata") val usageMetadata: UsageMetadata? = null,
) {
    fun extractText(): String? {
        return candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }
}

data class Candidate(
    @SerializedName("content") val content: Content
)

data class UsageMetadata(
    @SerializedName("promptTokenCount") val promptTokenCount: Int = 0,
    @SerializedName("candidatesTokenCount") val candidatesTokenCount: Int = 0,
    @SerializedName("totalTokenCount") val totalTokenCount: Int = 0,
)
