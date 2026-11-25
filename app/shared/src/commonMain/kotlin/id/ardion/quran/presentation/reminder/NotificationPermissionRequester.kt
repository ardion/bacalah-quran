package id.ardion.quran.presentation.reminder

import androidx.compose.runtime.Composable

fun interface NotificationPermissionRequester {
    fun request(onResult: (granted: Boolean) -> Unit)
}

@Composable
expect fun rememberNotificationPermissionRequester(): NotificationPermissionRequester
