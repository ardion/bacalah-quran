package id.ardion.quran.reminder

import id.ardion.quran.domain.model.ReminderEvent
import id.ardion.quran.domain.model.ReminderMode
import id.ardion.quran.domain.repository.ReminderScheduler
import id.ardion.quran.presentation.reminder.toMessage
import kotlinx.datetime.number
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

private const val MAX_EVENTS = 60
private const val WINDOW_DAYS = 12
private const val IDENTIFIER_PREFIX = "prayer_reminder_"
private const val ADZAN_SOUND = "adzan_clip.caf"

class IosReminderScheduler : ReminderScheduler {
    private val center = UNUserNotificationCenter.currentNotificationCenter()
    private val foregroundPresenter = ForegroundPresenter().also { center.delegate = it }

    override val isSupported: Boolean = true
    override val maxEvents: Int = MAX_EVENTS
    override val windowDays: Int = WINDOW_DAYS
    override val canScheduleExact: Boolean = true

    override suspend fun schedule(events: List<ReminderEvent>) {
        events.forEach { event ->
            val message = event.toMessage()
            val content = UNMutableNotificationContent().apply {
                setTitle(message.title)
                setBody(message.body)
                setSound(
                    if (event.mode == ReminderMode.ADZAN) UNNotificationSound.soundNamed(ADZAN_SOUND)
                    else UNNotificationSound.defaultSound()
                )
            }
            val components = NSDateComponents().apply {
                setYear(event.time.year.toLong())
                setMonth(event.time.month.number.toLong())
                setDay(event.time.day.toLong())
                setHour(event.time.hour.toLong())
                setMinute(event.time.minute.toLong())
            }
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = false)
            val request = UNNotificationRequest.requestWithIdentifier("$IDENTIFIER_PREFIX${event.id}", content, trigger)
            center.addNotificationRequest(request, withCompletionHandler = null)
        }
    }

    override fun cancelAll() {
        center.removePendingNotificationRequestsWithIdentifiers((0 until MAX_EVENTS).map { "$IDENTIFIER_PREFIX$it" })
    }

    override fun openExactAlarmSettings() = Unit
}

private class ForegroundPresenter : NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        withCompletionHandler(
            UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionList or UNNotificationPresentationOptionSound
        )
    }
}
