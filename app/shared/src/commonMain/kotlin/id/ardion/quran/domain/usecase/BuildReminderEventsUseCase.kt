package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.Prayer
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.model.ReminderEvent
import id.ardion.quran.domain.model.ReminderMode
import id.ardion.quran.domain.model.ReminderSettings
import id.ardion.quran.domain.model.timeOf
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

private const val HOUR_MINUTE_LENGTH = 5
private const val TIME_SEPARATOR = ':'
private const val MAX_HOUR = 23
private const val MAX_MINUTE = 59

class BuildReminderEventsUseCase {

    operator fun invoke(
        schedule: List<PrayerTimes>,
        settings: ReminderSettings,
        now: LocalDateTime,
        maxEvents: Int
    ): List<ReminderEvent> {
        if (!settings.isEnabled) return emptyList()

        return schedule
            .flatMap { day -> Prayer.entries.mapNotNull { prayer -> candidate(day, prayer, settings) } }
            .filter { it.time > now }
            .sortedBy { it.time }
            .take(maxEvents)
            .mapIndexed { index, event -> event.copy(id = index) }
    }

    private fun candidate(day: PrayerTimes, prayer: Prayer, settings: ReminderSettings): ReminderEvent? {
        val mode = settings.modeFor(prayer)
        if (mode == ReminderMode.OFF) return null
        val time = parseTime(day.timeOf(prayer)) ?: return null
        return ReminderEvent(
            id = 0,
            prayer = prayer,
            time = LocalDateTime(day.date, time),
            mode = mode,
            locationName = day.locationName
        )
    }

    private fun parseTime(raw: String): LocalTime? {
        val hourMinute = raw.trim().take(HOUR_MINUTE_LENGTH)
        val hour = hourMinute.substringBefore(TIME_SEPARATOR).toIntOrNull() ?: return null
        val minute = hourMinute.substringAfter(TIME_SEPARATOR, "").toIntOrNull() ?: return null
        if (hour !in 0..MAX_HOUR || minute !in 0..MAX_MINUTE) return null
        return LocalTime(hour, minute)
    }
}
