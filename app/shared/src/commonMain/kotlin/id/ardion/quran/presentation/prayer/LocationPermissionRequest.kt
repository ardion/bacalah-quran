package id.ardion.quran.presentation.prayer

import androidx.compose.runtime.Composable

@Composable
expect fun LocationPermissionRequest(onResult: (granted: Boolean) -> Unit)
