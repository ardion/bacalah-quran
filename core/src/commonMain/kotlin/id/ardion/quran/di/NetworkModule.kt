package id.ardion.quran.di

import id.ardion.quran.audio.AudioPlayer
import id.ardion.quran.network.createHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreNetworkModule = module {
    single(named("baseUrl")) { "https://api.alquran.cloud/v1/" }

    single {
        val baseUrl: String = get(named("baseUrl"))
        createHttpClient(baseUrl)
    }
}

val coreAudioModule = module {
    factory { AudioPlayer() }
}
