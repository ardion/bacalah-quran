package id.ardion.quran.presentation.prayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun LocationPermissionRequest(onPermissionGranted: () -> Unit) {
    LaunchedEffect(Unit) {
        onPermissionGranted()
    }
}
