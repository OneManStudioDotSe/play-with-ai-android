package se.onemanstudio.playaroundwithai.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import se.onemanstudio.playaroundwithai.core.config.di.GeminiApiKey
import timber.log.Timber
import javax.inject.Inject

class AuthenticationInterceptor @Inject constructor(
    @param:GeminiApiKey private val apiKey: String
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

        Timber.d("API request: ${originalRequest.method} ${originalHttpUrl.encodedPath} (API key attached)")

        val response = chain.proceed(newRequest)
        Timber.d("API response: ${response.code} for ${originalHttpUrl.encodedPath}")
        return response
    }
}
