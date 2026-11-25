package id.ardion.quran.presentation.prayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun LocationPermissionRequest(onResult: (granted: Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        onResult(true)
    }
}
