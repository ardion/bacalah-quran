package id.ardion.quran.domain.model

import kotlinx.datetime.LocalDateTime

enum class Prayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA }

enum class ReminderMode { OFF, NOTIFICATION, ADZAN }

data class ReminderSettings(
    val isEnabled: Boolean = false,
    val modes: Map<Prayer, ReminderMode> = Prayer.entries.associateWith { ReminderMode.NOTIFICATION }
) {
    fun modeFor(prayer: Prayer): ReminderMode = modes[prayer] ?: ReminderMode.NOTIFICATION
}

data class ReminderEvent(
    val id: Int,
    val prayer: Prayer,
    val time: LocalDateTime,
    val mode: ReminderMode,
    val locationName: String?
)

data class ReminderLocation(
    val coordinates: LocationCoordinates,
    val isLive: Boolean
)

fun PrayerTimes.timeOf(prayer: Prayer): String = when (prayer) {
    Prayer.FAJR -> fajr
    Prayer.DHUHR -> dhuhr
    Prayer.ASR -> asr
    Prayer.MAGHRIB -> maghrib
    Prayer.ISHA -> isha
}
