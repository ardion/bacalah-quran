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
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.json.Json

expect fun createHttpClient(baseUrl: String = "https://api.alquran.cloud/v1/"): HttpClient

fun HttpClientConfig<*>.configureHttpClient(baseUrl: String) {
    expectSuccess = true
    defaultRequest {
        url(baseUrl)
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.ALL
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
                is UnresolvedAddressException -> NetworkException.NoInternet
                else -> {
                    val className = exception::class.simpleName ?: ""
                    val causeName = exception.cause?.let { it::class.simpleName } ?: ""
                    if (className.contains("UnknownHost", ignoreCase = true) ||
                        className.contains("UnresolvedAddress", ignoreCase = true) ||
                        className.contains("Connect", ignoreCase = true) ||
                        className.contains("IO", ignoreCase = true) ||
                        causeName.contains("UnknownHost", ignoreCase = true) ||
                        causeName.contains("UnresolvedAddress", ignoreCase = true)
                    ) {
                        NetworkException.NoInternet
                    } else {
                        NetworkException.NoInternet
                    }
                }
            }
            throw domainException
        }
    }
}
