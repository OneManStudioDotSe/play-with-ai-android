package se.onemanstudio.playaroundwithai.data.gemini.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import se.onemanstudio.playaroundwithai.data.gemini.GeminiRequest
import se.onemanstudio.playaroundwithai.data.gemini.GeminiResponse

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash-latest:generateContent")
    suspend fun generateContent(@Query("key") apiKey: String, @Body request: GeminiRequest): GeminiResponse
}
