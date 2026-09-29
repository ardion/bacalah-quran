package id.ardion.quran.di

import id.ardion.quran.audio.AudioPlayer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreNetworkModule = module {
    single(named("baseUrl")) { "https://api.alquran.cloud/v1/" }

    single {
        val baseUrl: String = get(named("baseUrl"))
        HttpClient {
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
        }
    }
}

val coreAudioModule = module {
    factory { AudioPlayer() }
}
