package id.ardion.quran.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AladhanResponseDto(
    @SerialName("data") val data: AladhanDataDto? = null
)

@Serializable
data class AladhanDataDto(
    @SerialName("timings") val timings: TimingsDto? = null,
    @SerialName("date") val date: DateDto? = null
)

@Serializable
data class TimingsDto(
    @SerialName("Fajr") val fajr: String? = null,
    @SerialName("Sunrise") val sunrise: String? = null,
    @SerialName("Dhuhr") val dhuhr: String? = null,
    @SerialName("Asr") val asr: String? = null,
    @SerialName("Maghrib") val maghrib: String? = null,
    @SerialName("Isha") val isha: String? = null
)

@Serializable
data class DateDto(
    @SerialName("readable") val readable: String? = null,
    @SerialName("hijri") val hijri: HijriDateDto? = null
)

@Serializable
data class HijriDateDto(
    @SerialName("date") val date: String? = null,
    @SerialName("month") val month: HijriMonthDto? = null
)

@Serializable
data class HijriMonthDto(
    @SerialName("en") val en: String? = null
)

@Serializable
data class NominatimResponseDto(
    @SerialName("address") val address: NominatimAddressDto? = null
)

@Serializable
data class NominatimAddressDto(
    @SerialName("suburb") val suburb: String? = null,
    @SerialName("village") val village: String? = null,
    @SerialName("neighbourhood") val neighbourhood: String? = null,
    @SerialName("city_district") val cityDistrict: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("town") val town: String? = null
)

@Serializable
data class GeocodeResponseDto(
    @SerialName("locality") val locality: String? = null,
    @SerialName("city") val city: String? = null
)
