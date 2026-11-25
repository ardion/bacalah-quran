package id.ardion.quran.presentation.reminder

import bacalah.app.shared.generated.resources.Res
import bacalah.app.shared.generated.resources.prayer_asr
import bacalah.app.shared.generated.resources.prayer_dhuhr
import bacalah.app.shared.generated.resources.prayer_fajr
import bacalah.app.shared.generated.resources.prayer_isha
import bacalah.app.shared.generated.resources.prayer_maghrib
import bacalah.app.shared.generated.resources.reminder_notification_body
import bacalah.app.shared.generated.resources.reminder_notification_title
import id.ardion.quran.domain.model.Prayer
import id.ardion.quran.domain.model.ReminderEvent
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

data class ReminderMessage(val title: String, val body: String)

suspend fun ReminderEvent.toMessage(): ReminderMessage {
    val clock = formatClock(time.time)
    val prayerName = getString(prayer.nameResource())
    val body = locationName?.let { getString(Res.string.reminder_notification_body, clock, it) } ?: clock
    return ReminderMessage(title = getString(Res.string.reminder_notification_title, prayerName), body = body)
}

fun Prayer.nameResource(): StringResource = when (this) {
    Prayer.FAJR -> Res.string.prayer_fajr
    Prayer.DHUHR -> Res.string.prayer_dhuhr
    Prayer.ASR -> Res.string.prayer_asr
    Prayer.MAGHRIB -> Res.string.prayer_maghrib
    Prayer.ISHA -> Res.string.prayer_isha
}

private fun formatClock(time: LocalTime): String =
    "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
