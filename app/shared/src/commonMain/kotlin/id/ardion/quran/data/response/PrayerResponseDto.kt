package id.ardion.quran.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AladhanResponseDto(
    val code: Int,
    val status: String,
    val data: AladhanDataDto? = null
)

@Serializable
data class AladhanDataDto(
    val timings: TimingsDto,
    val date: DateDto? = null
)

@Serializable
data class TimingsDto(
    @SerialName("Fajr") val fajr: String,
    @SerialName("Sunrise") val sunrise: String,
    @SerialName("Dhuhr") val dhuhr: String,
    @SerialName("Asr") val asr: String,
    @SerialName("Sunset") val sunset: String,
    @SerialName("Maghrib") val maghrib: String,
    @SerialName("Isha") val isha: String,
    @SerialName("Imsak") val imsak: String? = null
)

@Serializable
data class DateDto(
    val readable: String? = null,
    val hijri: HijriDateDto? = null
)

@Serializable
data class HijriDateDto(
    val date: String? = null,
    val month: HijriMonthDto? = null,
    val year: String? = null
)

@Serializable
data class HijriMonthDto(
    val number: Int? = null,
    val en: String? = null,
    val ar: String? = null
)

@Serializable
data class NominatimResponseDto(
    val address: NominatimAddressDto? = null,
    val display_name: String? = null
)

@Serializable
data class NominatimAddressDto(
    val suburb: String? = null,
    val village: String? = null,
    val neighbourhood: String? = null,
    val city_district: String? = null,
    val city: String? = null,
    val town: String? = null,
    val county: String? = null,
    val state: String? = null
)

@Serializable
data class GeocodeResponseDto(
    val locality: String? = null,
    val city: String? = null,
    val principalSubdivision: String? = null,
    val countryName: String? = null
)
