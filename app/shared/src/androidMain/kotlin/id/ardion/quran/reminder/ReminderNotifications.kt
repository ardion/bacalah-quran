package id.ardion.quran.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import id.ardion.quran.app.shared.R
import id.ardion.quran.domain.model.ReminderMode

private const val CHANNEL_NOTIFICATION = "prayer_reminder"
private const val CHANNEL_ADZAN = "prayer_adzan"
private const val OPEN_APP_REQUEST_CODE = 0

internal fun ensureReminderChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(NotificationManager::class.java) ?: return

    val notification = NotificationChannel(
        CHANNEL_NOTIFICATION,
        context.getString(R.string.reminder_channel_notification),
        NotificationManager.IMPORTANCE_HIGH
    )
    val adzan = NotificationChannel(
        CHANNEL_ADZAN,
        context.getString(R.string.reminder_channel_adzan),
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        setSound(
            Uri.parse("android.resource://${context.packageName}/${R.raw.adzan_clip}"),
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
    }
    manager.createNotificationChannels(listOf(notification, adzan))
}

internal fun showReminderNotification(context: Context, id: Int, title: String, body: String, mode: ReminderMode) {
    if (!canPostNotifications(context)) return
    ensureReminderChannels(context)

    val notification = NotificationCompat.Builder(context, if (mode == ReminderMode.ADZAN) CHANNEL_ADZAN else CHANNEL_NOTIFICATION)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(body)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(openAppIntent(context))
        .build()
    NotificationManagerCompat.from(context).notify(id, notification)
}

internal fun canPostNotifications(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun openAppIntent(context: Context): PendingIntent? =
    context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launch ->
        PendingIntent.getActivity(context, OPEN_APP_REQUEST_CODE, launch, PendingIntent.FLAG_IMMUTABLE)
    }
