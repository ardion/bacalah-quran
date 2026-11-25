package id.ardion.quran.di

import id.ardion.quran.data.local.KeyValueStore
import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.domain.repository.LocationProvider
import id.ardion.quran.domain.repository.ReminderScheduler
import id.ardion.quran.platform.CompassSensor
import id.ardion.quran.platform.FileResponseCache
import id.ardion.quran.platform.PreferencesKeyValueStore
import id.ardion.quran.platform.UnavailableCompassSensor
import id.ardion.quran.platform.UnavailableLocationProvider
import id.ardion.quran.platform.UnavailableReminderScheduler
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

private const val RESPONSE_CACHE_FOLDER = ".bacalah/quran-cache"

internal actual val platformModule: Module = module {
    single<ReminderScheduler> { UnavailableReminderScheduler() }
    single<KeyValueStore> { PreferencesKeyValueStore() }
    single<ResponseCache> { FileResponseCache(File(System.getProperty("user.home"), RESPONSE_CACHE_FOLDER)) }
    single<LocationProvider> { UnavailableLocationProvider() }
    factory<CompassSensor> { UnavailableCompassSensor() }
}
