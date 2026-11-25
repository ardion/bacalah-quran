package id.ardion.quran.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import id.ardion.quran.domain.model.ReminderEvent
import id.ardion.quran.domain.repository.ReminderScheduler
import id.ardion.quran.logging.logWarning
import id.ardion.quran.presentation.reminder.toMessage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

private const val TAG = "ReminderScheduler"
private const val WINDOW_DAYS = 7
private const val MAX_REQUEST_CODES = 64
private const val INEXACT_WINDOW_MS = 60_000L

internal const val EXTRA_ID = "reminder_id"
internal const val EXTRA_TITLE = "reminder_title"
internal const val EXTRA_BODY = "reminder_body"
internal const val EXTRA_MODE = "reminder_mode"

class AndroidReminderScheduler(private val context: Context) : ReminderScheduler {

    private val alarmManager: AlarmManager? = context.getSystemService(AlarmManager::class.java)

    override val isSupported: Boolean = alarmManager != null
    override val maxEvents: Int = MAX_REQUEST_CODES
    override val windowDays: Int = WINDOW_DAYS

    override val canScheduleExact: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager?.canScheduleExactAlarms() == true

    @OptIn(ExperimentalTime::class)
    override suspend fun schedule(events: List<ReminderEvent>) {
        val manager = alarmManager ?: return
        ensureReminderChannels(context)
        events.forEach { event ->
            val message = event.toMessage()
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
                .putExtra(EXTRA_ID, event.id)
                .putExtra(EXTRA_TITLE, message.title)
                .putExtra(EXTRA_BODY, message.body)
                .putExtra(EXTRA_MODE, event.mode.name)
            val pending = PendingIntent.getBroadcast(
                context,
                event.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAt = event.time.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            setAlarm(manager, triggerAt, pending)
        }
    }

    override fun cancelAll() {
        val manager = alarmManager ?: return
        repeat(MAX_REQUEST_CODES) { requestCode ->
            PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, PrayerAlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )?.let { pending ->
                manager.cancel(pending)
                pending.cancel()
            }
        }
    }

    override fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun setAlarm(manager: AlarmManager, triggerAt: Long, pending: PendingIntent) {
        if (!canScheduleExact) {
            manager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, INEXACT_WINDOW_MS, pending)
            return
        }
        try {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } catch (e: SecurityException) {
            logWarning(TAG, "Exact alarm permission revoked, using an inexact window", e)
            manager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, INEXACT_WINDOW_MS, pending)
        }
    }
}
