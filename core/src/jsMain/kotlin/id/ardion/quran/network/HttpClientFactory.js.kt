package id.ardion.quran.network

import org.koin.core.scope.Scope
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

internal actual fun Scope.createPlatformHttpClient(baseUrl: String): HttpClient {
    return HttpClient(Js) {
        configureHttpClient(baseUrl)
    }
}
