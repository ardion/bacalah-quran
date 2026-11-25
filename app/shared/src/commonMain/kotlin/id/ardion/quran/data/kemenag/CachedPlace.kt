package id.ardion.quran.data.kemenag

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CachedPlace(
    @SerialName("regency") val regency: Regency? = null,
    @SerialName("locationName") val locationName: String? = null,
    @SerialName("isInIndonesia") val isInIndonesia: Boolean = false
)
