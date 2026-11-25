package id.ardion.quran.di

import id.ardion.quran.data.local.KeyValueStore
import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.domain.repository.LocationProvider
import id.ardion.quran.platform.CompassSensor
import id.ardion.quran.platform.FileResponseCache
import id.ardion.quran.platform.IosCompassSensor
import id.ardion.quran.platform.IosLocationProvider
import id.ardion.quran.platform.UserDefaultsKeyValueStore
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single<KeyValueStore> { UserDefaultsKeyValueStore() }
    single<ResponseCache> { FileResponseCache() }
    single<LocationProvider> { IosLocationProvider() }
    factory<CompassSensor> { IosCompassSensor() }
}
