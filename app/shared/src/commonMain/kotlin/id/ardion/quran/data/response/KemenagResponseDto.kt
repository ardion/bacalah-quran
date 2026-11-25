package id.ardion.quran.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KemenagCitySearchDto(
    @SerialName("status") val status: Boolean? = null,
    @SerialName("data") val data: List<KemenagCityDto> = emptyList()
)

@Serializable
data class KemenagCityDto(
    @SerialName("id") val id: String? = null,
    @SerialName("lokasi") val name: String? = null
)

@Serializable
data class KemenagScheduleResponseDto(
    @SerialName("status") val status: Boolean? = null,
    @SerialName("data") val data: KemenagScheduleDto? = null
)

@Serializable
data class KemenagScheduleDto(
    @SerialName("lokasi") val cityName: String? = null,
    @SerialName("jadwal") val days: List<KemenagDayDto> = emptyList()
)

@Serializable
data class KemenagDayDto(
    @SerialName("date") val date: String? = null,
    @SerialName("subuh") val fajr: String? = null,
    @SerialName("terbit") val sunrise: String? = null,
    @SerialName("dzuhur") val dhuhr: String? = null,
    @SerialName("ashar") val asr: String? = null,
    @SerialName("maghrib") val maghrib: String? = null,
    @SerialName("isya") val isha: String? = null
)
