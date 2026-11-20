package id.ardion.quran.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClient(baseUrl: String): HttpClient {
    return HttpClient(Darwin) {
        configureHttpClient(baseUrl)
    }
}
