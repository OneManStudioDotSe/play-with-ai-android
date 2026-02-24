package se.onemanstudio.playaroundwithai.core.network.dto

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents") val contents: List<Content>
)

data class Content(
    @SerializedName("parts") val parts: List<Part>
)

data class Part(
    @SerializedName("text") val text: String? = null,
    @SerializedName("inline_data") val inlineData: ImageData? = null,
)

data class ImageData(
    @SerializedName("mime_type") val mimeType: String,
    @SerializedName("data") val data: String
)
