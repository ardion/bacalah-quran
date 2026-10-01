package id.ardion.quran.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.window

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    if (enabled) {
        val currentOnBack by rememberUpdatedState(onBack)

        DisposableEffect(enabled) {
            window.history.pushState(null, "", "")

            val previousOnPopState = window.onpopstate

            window.onpopstate = {
                currentOnBack()
            }

            onDispose {
                window.onpopstate = previousOnPopState
            }
        }
    }
}
