package id.ardion.quran.data.local

import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.Prayer
import id.ardion.quran.domain.model.ReminderLocation
import id.ardion.quran.domain.model.ReminderMode
import id.ardion.quran.domain.model.ReminderSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReminderSettingsEntity(
    @SerialName("isEnabled") val isEnabled: Boolean = false,
    @SerialName("modes") val modes: Map<String, String> = emptyMap()
)

@Serializable
internal data class ReminderLocationEntity(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("isLive") val isLive: Boolean
)

internal fun ReminderSettingsEntity.toDomain(): ReminderSettings = ReminderSettings(
    isEnabled = isEnabled,
    modes = Prayer.entries.associateWith { prayer ->
        ReminderMode.entries.firstOrNull { it.name == modes[prayer.name] } ?: ReminderMode.NOTIFICATION
    }
)

internal fun ReminderSettings.toEntity() = ReminderSettingsEntity(
    isEnabled = isEnabled,
    modes = Prayer.entries.associate { it.name to modeFor(it).name }
)

internal fun ReminderLocationEntity.toDomain() =
    ReminderLocation(LocationCoordinates(latitude, longitude), isLive)

internal fun ReminderLocation.toEntity() =
    ReminderLocationEntity(coordinates.latitude, coordinates.longitude, isLive)
