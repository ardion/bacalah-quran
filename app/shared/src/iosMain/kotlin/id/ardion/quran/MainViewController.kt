package id.ardion.quran

import androidx.compose.ui.window.ComposeUIViewController
import id.ardion.quran.di.initKoin
import id.ardion.quran.logging.logWarning
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun MainViewController() = ComposeUIViewController(configure = { initKoin() }) {
    setUnhandledExceptionHook { throwable ->
        logWarning("Uncaught", "Unhandled Kotlin exception", throwable)
    }
    App()
}
