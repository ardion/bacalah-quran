package id.ardion.quran.platform

import id.ardion.quran.domain.model.ReminderEvent
import id.ardion.quran.domain.repository.ReminderScheduler

class UnavailableReminderScheduler : ReminderScheduler {
    override val isSupported: Boolean = false
    override val maxEvents: Int = 0
    override val windowDays: Int = 0
    override val canScheduleExact: Boolean = false
    override suspend fun schedule(events: List<ReminderEvent>) = Unit
    override fun cancelAll() = Unit
    override fun openExactAlarmSettings() = Unit
}
