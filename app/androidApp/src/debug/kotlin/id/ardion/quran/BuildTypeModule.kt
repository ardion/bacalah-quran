package id.ardion.quran

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.Interceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

private const val CHUCKER_MAX_CONTENT_BYTES = 250_000L

val buildTypeModule: Module = module {
    single<Interceptor> {
        val context = androidContext()
        ChuckerInterceptor.Builder(context)
            .collector(ChuckerCollector(context, showNotification = true, retentionPeriod = RetentionManager.Period.ONE_HOUR))
            .maxContentLength(CHUCKER_MAX_CONTENT_BYTES)
            .alwaysReadResponseBody(true)
            .build()
    }
}
