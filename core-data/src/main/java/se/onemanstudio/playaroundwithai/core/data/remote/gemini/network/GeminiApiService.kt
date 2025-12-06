package se.onemanstudio.playaroundwithai.core.data.remote.gemini.network

import retrofit2.http.Body
import retrofit2.http.POST
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.GeminiRequest
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.GeminiResponse

interface GeminiApiService {
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun generateContent(@Body request: GeminiRequest): GeminiResponse
}
