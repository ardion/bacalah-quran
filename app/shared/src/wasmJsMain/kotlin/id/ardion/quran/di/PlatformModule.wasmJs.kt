package id.ardion.quran.di

import id.ardion.quran.data.local.KeyValueStore
import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.domain.repository.LocationProvider
import id.ardion.quran.platform.CompassSensor
import id.ardion.quran.platform.LocalStorageKeyValueStore
import id.ardion.quran.platform.LocalStorageResponseCache
import id.ardion.quran.platform.UnavailableCompassSensor
import id.ardion.quran.platform.UnavailableLocationProvider
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single<KeyValueStore> { LocalStorageKeyValueStore() }
    single<ResponseCache> { LocalStorageResponseCache() }
    single<LocationProvider> { UnavailableLocationProvider() }
    factory<CompassSensor> { UnavailableCompassSensor() }
}
