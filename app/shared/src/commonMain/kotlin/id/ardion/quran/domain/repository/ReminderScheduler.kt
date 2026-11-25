package id.ardion.quran.domain.repository

import id.ardion.quran.domain.model.ReminderEvent

interface ReminderScheduler {
    val isSupported: Boolean
    val maxEvents: Int
    val windowDays: Int
    val canScheduleExact: Boolean
    suspend fun schedule(events: List<ReminderEvent>)
    fun cancelAll()
    fun openExactAlarmSettings()
}
