package id.ardion.quran.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    if (enabled) {
        DisposableEffect(Unit) {
            window.history.pushState(null, "", "")

            val previousOnPopState = window.onpopstate

            window.onpopstate = {
                onBack()
            }

            onDispose {
                window.onpopstate = previousOnPopState
            }
        }
    }
}
