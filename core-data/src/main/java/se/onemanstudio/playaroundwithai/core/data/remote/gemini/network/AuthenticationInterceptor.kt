package se.onemanstudio.playaroundwithai.core.data.remote.gemini.network

import okhttp3.Interceptor
import okhttp3.Response
import se.onemanstudio.playaroundwithai.core.data.di.GeminiApiKey
import javax.inject.Inject

class AuthenticationInterceptor @Inject constructor(
    @GeminiApiKey private val apiKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url

        val newUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("key", apiKey)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}