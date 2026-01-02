package se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiResponse

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash-lite:generateContent")
    suspend fun generateContent(@Body request: GeminiRequest): GeminiResponse
}
