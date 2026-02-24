package se.onemanstudio.playaroundwithai.core.network.api

import retrofit2.http.Body
import retrofit2.http.POST
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiResponse

interface GeminiApiService {
    @POST("v1beta/models/gemini-3-flash-preview:generateContent")
    suspend fun generateContent(@Body request: GeminiRequest): GeminiResponse
}
