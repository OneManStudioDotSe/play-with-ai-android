package se.onemanstudio.playaroundwithai.core.network.dto

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents") val contents: List<Content>,
    @SerializedName("tools") val tools: List<Tool>? = null,
)

data class Content(
    @SerializedName("role") val role: String? = null,
    @SerializedName("parts") val parts: List<Part>,
)

data class Part(
    @SerializedName("text") val text: String? = null,
    @SerializedName("inline_data") val inlineData: ImageData? = null,
    @SerializedName("functionCall") val functionCall: FunctionCallDto? = null,
    @SerializedName("functionResponse") val functionResponse: FunctionResponseDto? = null,
    @SerializedName("thought_signature") val thoughtSignature: String? = null,
)

data class ImageData(
    @SerializedName("mime_type") val mimeType: String,
    @SerializedName("data") val data: String,
)

data class FunctionCallDto(
    @SerializedName("name") val name: String,
    @SerializedName("args") val args: Map<String, Any>,
)

data class FunctionResponseDto(
    @SerializedName("name") val name: String,
    @SerializedName("response") val response: Map<String, Any>,
)

data class Tool(
    @SerializedName("functionDeclarations") val functionDeclarations: List<FunctionDeclaration>,
)

data class FunctionDeclaration(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("parameters") val parameters: FunctionParameters,
)

data class FunctionParameters(
    @SerializedName("type") val type: String,
    @SerializedName("properties") val properties: Map<String, PropertySchema>,
    @SerializedName("required") val required: List<String>? = null,
)

data class PropertySchema(
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("items") val items: PropertySchema? = null,
    @SerializedName("properties") val properties: Map<String, PropertySchema>? = null,
    @SerializedName("required") val required: List<String>? = null,
)
