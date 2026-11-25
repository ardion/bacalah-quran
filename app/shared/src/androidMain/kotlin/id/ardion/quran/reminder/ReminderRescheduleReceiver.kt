package id.ardion.quran.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import id.ardion.quran.domain.usecase.ScheduleRemindersUseCase
import id.ardion.quran.logging.logWarning
import id.ardion.quran.result.runCatchingCancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

private const val TAG = "ReminderReschedule"
private val rescheduleScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        rescheduleReminders(goAsync())
    }
}

internal fun rescheduleReminders(pending: BroadcastReceiver.PendingResult) {
    rescheduleScope.launch {
        runCatchingCancellable {
            KoinPlatformTools.defaultContext().getOrNull()?.get<ScheduleRemindersUseCase>()?.invoke()
        }.onFailure { logWarning(TAG, "Rescheduling reminders failed", it) }
        pending.finish()
    }
}
