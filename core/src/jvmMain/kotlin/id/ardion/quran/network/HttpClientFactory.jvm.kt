package id.ardion.quran.network

import io.ktor.client.HttpClient

actual fun createHttpClient(baseUrl: String): HttpClient {
    return HttpClient {
        configureHttpClient(baseUrl)
    }
}
