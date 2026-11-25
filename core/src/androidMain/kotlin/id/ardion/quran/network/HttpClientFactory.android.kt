package id.ardion.quran.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Interceptor
import org.koin.core.scope.Scope

internal actual fun Scope.createPlatformHttpClient(baseUrl: String): HttpClient {
    val interceptors = getAll<Interceptor>()
    return HttpClient(OkHttp) {
        engine { interceptors.forEach(::addInterceptor) }
        configureHttpClient(baseUrl)
    }
}
