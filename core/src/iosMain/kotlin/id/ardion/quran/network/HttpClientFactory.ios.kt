package id.ardion.quran.network

import org.koin.core.scope.Scope
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual fun Scope.createPlatformHttpClient(baseUrl: String): HttpClient {
    return HttpClient(Darwin) {
        configureHttpClient(baseUrl)
    }
}
