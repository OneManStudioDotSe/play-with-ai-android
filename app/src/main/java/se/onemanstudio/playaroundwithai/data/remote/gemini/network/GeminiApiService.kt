package se.onemanstudio.playaroundwithai.data.remote.gemini.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import se.onemanstudio.playaroundwithai.data.remote.gemini.GeminiRequest
import se.onemanstudio.playaroundwithai.data.remote.gemini.GeminiResponse

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash-lite:generateContent")
    suspend fun generateContent(@Query("key") apiKey: String, @Body request: GeminiRequest): GeminiResponse
}
