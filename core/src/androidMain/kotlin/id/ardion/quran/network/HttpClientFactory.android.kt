package id.ardion.quran.network

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import id.ardion.quran.utils.ContextHolder
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpClient(baseUrl: String): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            ContextHolder.appContext?.let { context ->
                val chuckerCollector = ChuckerCollector(
                    context = context,
                    showNotification = true,
                    retentionPeriod = RetentionManager.Period.ONE_HOUR
                )
                val chuckerInterceptor = ChuckerInterceptor.Builder(context)
                    .collector(chuckerCollector)
                    .maxContentLength(250_000L)
                    .alwaysReadResponseBody(true)
                    .build()
                addInterceptor(chuckerInterceptor)
            }
        }
        configureHttpClient(baseUrl)
    }
}
