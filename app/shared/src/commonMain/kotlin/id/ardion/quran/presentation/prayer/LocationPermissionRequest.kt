package id.ardion.quran.presentation.prayer

import androidx.compose.runtime.Composable

@Composable
expect fun LocationPermissionRequest(onPermissionGranted: () -> Unit)
