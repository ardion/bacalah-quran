package id.ardion.quran

import androidx.compose.ui.window.ComposeUIViewController
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun MainViewController() = ComposeUIViewController {
    setUnhandledExceptionHook { throwable ->
        println("🔥 UNCAUGHT KOTLIN EXCEPTION: ${throwable::class.simpleName}: ${throwable.message}")
        throwable.printStackTrace()
    }
    App()
}


