package id.ardion.quran.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import id.ardion.quran.domain.model.ReminderMode

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE)
        val body = intent.getStringExtra(EXTRA_BODY)
        if (title != null && body != null) {
            val mode = ReminderMode.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_MODE) }
                ?: ReminderMode.NOTIFICATION
            showReminderNotification(context, intent.getIntExtra(EXTRA_ID, 0), title, body, mode)
        }
        rescheduleReminders(goAsync())
    }
}
