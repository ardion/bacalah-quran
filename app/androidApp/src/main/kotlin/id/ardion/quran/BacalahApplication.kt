package id.ardion.quran

import android.app.Application
import id.ardion.quran.di.initAndroidKoin

class BacalahApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initAndroidKoin(this, buildTypeModule)
    }
}
