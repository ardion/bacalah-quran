package id.ardion.quran.network

import org.koin.core.scope.Scope
import io.ktor.client.HttpClient

internal actual fun Scope.createPlatformHttpClient(baseUrl: String): HttpClient {
    return HttpClient {
        configureHttpClient(baseUrl)
    }
}
