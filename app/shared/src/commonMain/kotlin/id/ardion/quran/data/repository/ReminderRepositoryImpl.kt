package id.ardion.quran.data.repository

import id.ardion.quran.data.local.KeyValueStore
import id.ardion.quran.data.local.ReminderLocationEntity
import id.ardion.quran.data.local.ReminderSettingsEntity
import id.ardion.quran.data.local.toDomain
import id.ardion.quran.data.local.toEntity
import id.ardion.quran.domain.model.ReminderLocation
import id.ardion.quran.domain.model.ReminderSettings
import id.ardion.quran.domain.repository.ReminderRepository
import id.ardion.quran.logging.logWarning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private const val TAG = "ReminderRepository"
internal const val KEY_REMINDER_SETTINGS = "key_reminder_settings"
internal const val KEY_REMINDER_LOCATION = "key_reminder_location"

class ReminderRepositoryImpl(private val store: KeyValueStore) : ReminderRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val _settings = MutableStateFlow(
        read(KEY_REMINDER_SETTINGS, ReminderSettingsEntity.serializer())?.toDomain() ?: ReminderSettings()
    )
    override val settings: StateFlow<ReminderSettings> = _settings.asStateFlow()

    override fun saveSettings(settings: ReminderSettings) {
        store.putString(KEY_REMINDER_SETTINGS, json.encodeToString(ReminderSettingsEntity.serializer(), settings.toEntity()))
        _settings.value = settings
    }

    override fun lastLocation(): ReminderLocation? =
        read(KEY_REMINDER_LOCATION, ReminderLocationEntity.serializer())?.toDomain()

    override fun saveLastLocation(location: ReminderLocation) {
        store.putString(KEY_REMINDER_LOCATION, json.encodeToString(ReminderLocationEntity.serializer(), location.toEntity()))
    }

    private fun <T> read(key: String, serializer: KSerializer<T>): T? {
        val raw = store.getString(key) ?: return null
        return try {
            json.decodeFromString(serializer, raw)
        } catch (e: IllegalArgumentException) {
            logWarning(TAG, "Discarding unreadable value for $key", e)
            store.remove(key)
            null
        }
    }
}
