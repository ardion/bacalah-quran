package id.ardion.quran.network

import id.ardion.quran.exception.NetworkException
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.scope.Scope

internal expect fun Scope.createPlatformHttpClient(baseUrl: String): HttpClient

internal fun HttpClientConfig<*>.configureHttpClient(baseUrl: String) {
    expectSuccess = true
    defaultRequest {
        url(baseUrl)
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        })
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            val domainException = when (exception) {
                is ClientRequestException -> {
                    when (exception.response.status.value) {
                        401 -> NetworkException.Unauthorized
                        403 -> NetworkException.Forbidden
                        404 -> NetworkException.NotFound
                        else -> NetworkException.ClientError(exception.response.status.value)
                    }
                }
                is ServerResponseException -> NetworkException.ServerError(exception.response.status.value)
                is SocketTimeoutException, is ConnectTimeoutException -> NetworkException.Timeout
                else -> NetworkException.NoInternet
            }
            throw domainException
        }
    }
}
