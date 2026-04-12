package se.onemanstudio.playaroundwithai.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import se.onemanstudio.playaroundwithai.core.config.settings.AppSettingsHolder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeoutInterceptor @Inject constructor(
    private val appSettingsHolder: AppSettingsHolder
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val timeoutSec = appSettingsHolder.networkTimeoutSeconds.value
        return chain
            .withConnectTimeout(timeoutSec, TimeUnit.SECONDS)
            .withReadTimeout(timeoutSec, TimeUnit.SECONDS)
            .withWriteTimeout(timeoutSec, TimeUnit.SECONDS)
            .proceed(chain.request())
    }
}
