package id.ardion.quran.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

fun initAndroidKoin(context: Context, vararg extraModules: Module) {
    initKoin {
        androidContext(context.applicationContext)
        modules(*extraModules)
    }
}
