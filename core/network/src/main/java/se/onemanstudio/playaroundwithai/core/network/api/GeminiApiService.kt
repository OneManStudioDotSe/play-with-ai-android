package se.onemanstudio.playaroundwithai.core.network.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiResponse

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(@Path("model") model: String, @Body request: GeminiRequest): GeminiResponse
}
