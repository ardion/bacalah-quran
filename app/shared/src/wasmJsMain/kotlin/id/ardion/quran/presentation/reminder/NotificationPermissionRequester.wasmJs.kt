package id.ardion.quran.presentation.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberNotificationPermissionRequester(): NotificationPermissionRequester =
    remember { NotificationPermissionRequester { onResult -> onResult(false) } }
