package id.ardion.quran.di

import id.ardion.quran.audio.AudioPlayback
import id.ardion.quran.audio.AudioPlayer
import id.ardion.quran.audio.AudioQueueController
import id.ardion.quran.config.ApiConfig
import id.ardion.quran.network.createPlatformHttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val coreNetworkModule = module {
    single { createPlatformHttpClient(ApiConfig.QURAN_BASE_URL) }
}

val coreAudioModule = module {
    factory<AudioPlayback> { AudioPlayer() }
    factoryOf(::AudioQueueController)
}
