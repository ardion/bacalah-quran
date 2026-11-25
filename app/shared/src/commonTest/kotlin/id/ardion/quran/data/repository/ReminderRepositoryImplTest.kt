package id.ardion.quran.data.repository

import id.ardion.quran.data.local.KeyValueStore
import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.Prayer
import id.ardion.quran.domain.model.ReminderLocation
import id.ardion.quran.domain.model.ReminderMode
import id.ardion.quran.domain.model.ReminderSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

private class InMemoryKeyValueStore(initial: Map<String, String> = emptyMap()) : KeyValueStore {
    val values = initial.toMutableMap()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) { values[key] = value }
    override fun remove(key: String) { values.remove(key) }
}

class ReminderRepositoryImplTest {

    @Test
    fun defaultsAreDisabledWithNotificationModes() {
        val settings = ReminderRepositoryImpl(InMemoryKeyValueStore()).settings.value
        assertFalse(settings.isEnabled)
        assertEquals(Prayer.entries.associateWith { ReminderMode.NOTIFICATION }, settings.modes)
    }

    @Test
    fun settingsSurviveRestart() {
        val store = InMemoryKeyValueStore()
        val saved = ReminderSettings(
            isEnabled = true,
            modes = Prayer.entries.associateWith { ReminderMode.NOTIFICATION } + (Prayer.MAGHRIB to ReminderMode.ADZAN)
        )
        ReminderRepositoryImpl(store).saveSettings(saved)

        assertEquals(saved, ReminderRepositoryImpl(store).settings.value)
    }

    @Test
    fun unknownModeNameFallsBackToNotification() {
        val store = InMemoryKeyValueStore(
            mapOf(KEY_REMINDER_SETTINGS to """{"isEnabled":true,"modes":{"FAJR":"LOUD","ISHA":"ADZAN"}}""")
        )
        val settings = ReminderRepositoryImpl(store).settings.value
        assertEquals(ReminderMode.NOTIFICATION, settings.modeFor(Prayer.FAJR))
        assertEquals(ReminderMode.ADZAN, settings.modeFor(Prayer.ISHA))
    }

    @Test
    fun corruptJsonFallsBackToDefaults() {
        val store = InMemoryKeyValueStore(mapOf(KEY_REMINDER_SETTINGS to "{broken"))
        assertEquals(ReminderSettings(), ReminderRepositoryImpl(store).settings.value)
    }

    @Test
    fun lastLocationRoundTrips() {
        val store = InMemoryKeyValueStore()
        assertNull(ReminderRepositoryImpl(store).lastLocation())

        val location = ReminderLocation(LocationCoordinates(-6.595, 106.8166), isLive = true)
        ReminderRepositoryImpl(store).saveLastLocation(location)
        assertEquals(location, ReminderRepositoryImpl(store).lastLocation())
    }
}
