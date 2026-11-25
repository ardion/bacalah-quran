package id.ardion.quran.domain.repository

import id.ardion.quran.domain.model.ReminderLocation
import id.ardion.quran.domain.model.ReminderSettings
import kotlinx.coroutines.flow.StateFlow

interface ReminderRepository {
    val settings: StateFlow<ReminderSettings>
    fun saveSettings(settings: ReminderSettings)
    fun lastLocation(): ReminderLocation?
    fun saveLastLocation(location: ReminderLocation)
}
