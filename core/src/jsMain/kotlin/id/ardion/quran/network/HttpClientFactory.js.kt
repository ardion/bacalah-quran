package id.ardion.quran.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun createHttpClient(baseUrl: String): HttpClient {
    return HttpClient(Js) {
        configureHttpClient(baseUrl)
    }
}
