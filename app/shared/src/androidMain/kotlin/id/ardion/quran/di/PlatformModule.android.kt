package id.ardion.quran.di

import id.ardion.quran.data.local.KeyValueStore
import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.domain.repository.LocationProvider
import id.ardion.quran.domain.repository.ReminderScheduler
import id.ardion.quran.platform.AndroidCompassSensor
import id.ardion.quran.platform.AndroidKeyValueStore
import id.ardion.quran.platform.AndroidLocationProvider
import id.ardion.quran.platform.CompassSensor
import id.ardion.quran.platform.FileResponseCache
import org.koin.android.ext.koin.androidContext
import id.ardion.quran.platform.UnavailableReminderScheduler
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

private const val RESPONSE_CACHE_FOLDER = "quran-cache"

internal actual val platformModule: Module = module {
    single<ReminderScheduler> { UnavailableReminderScheduler() }
    single<KeyValueStore> { AndroidKeyValueStore(androidContext()) }
    single<ResponseCache> { FileResponseCache(File(androidContext().filesDir, RESPONSE_CACHE_FOLDER)) }
    single<LocationProvider> { AndroidLocationProvider(androidContext()) }
    factory<CompassSensor> { AndroidCompassSensor(androidContext()) }
}
