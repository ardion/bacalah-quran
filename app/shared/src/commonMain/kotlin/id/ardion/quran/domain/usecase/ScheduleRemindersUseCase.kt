package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.repository.PrayerRepository
import id.ardion.quran.domain.repository.ReminderRepository
import id.ardion.quran.domain.repository.ReminderScheduler
import id.ardion.quran.domain.time.NowProvider

class ScheduleRemindersUseCase(
    private val repository: ReminderRepository,
    private val prayerRepository: PrayerRepository,
    private val scheduler: ReminderScheduler,
    private val buildEvents: BuildReminderEventsUseCase,
    private val nowProvider: NowProvider
) {
    suspend operator fun invoke() {
        if (!scheduler.isSupported) return

        val settings = repository.settings.value
        val location = repository.lastLocation()
        if (!settings.isEnabled || location == null) {
            scheduler.cancelAll()
            return
        }

        val now = nowProvider.now()
        val schedule = prayerRepository
            .getSchedule(location.coordinates, location.isLive, now.date, scheduler.windowDays)
            .getOrNull()
            ?: return

        scheduler.cancelAll()
        scheduler.schedule(buildEvents(schedule, settings, now, scheduler.maxEvents))
    }
}
