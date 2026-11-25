package id.ardion.quran.data.mapper

import id.ardion.quran.data.response.AladhanDataDto
import id.ardion.quran.data.response.GeocodeResponseDto
import id.ardion.quran.data.response.NominatimAddressDto
import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.PrayerTimes

const val MISSING_TIME = "--:--"
private const val MISSING_DATE = "-"

fun AladhanDataDto.toDomain(
    location: LocationCoordinates,
    qiblaBearing: Double,
    locationName: String?,
    isLiveLocation: Boolean
): PrayerTimes? {
    val times = timings ?: return null
    return PrayerTimes(
        fajr = times.fajr ?: MISSING_TIME,
        sunrise = times.sunrise ?: MISSING_TIME,
        dhuhr = times.dhuhr ?: MISSING_TIME,
        asr = times.asr ?: MISSING_TIME,
        maghrib = times.maghrib ?: MISSING_TIME,
        isha = times.isha ?: MISSING_TIME,
        dateReadable = date?.readable ?: MISSING_DATE,
        hijriDate = listOfNotNull(date?.hijri?.date, date?.hijri?.month?.en).joinToString(" "),
        latitude = location.latitude,
        longitude = location.longitude,
        qiblaBearing = qiblaBearing,
        locationName = locationName,
        isLiveLocation = isLiveLocation
    )
}

private val administrativePrefixes = listOf("Kelurahan ", "Kecamatan ", "Kota ", "Kabupaten ")

private fun String?.withoutAdministrativePrefix(): String =
    administrativePrefixes.fold(this.orEmpty()) { name, prefix -> name.replace(prefix, "") }.trim()

fun NominatimAddressDto.formatLocationName(): String? {
    val kelurahan = (suburb ?: village ?: neighbourhood).withoutAdministrativePrefix()
    val kota = (city ?: town ?: cityDistrict).withoutAdministrativePrefix()
    return when {
        kelurahan.isBlank() -> null
        kota.isNotBlank() && kelurahan != kota -> "$kelurahan, $kota"
        else -> kelurahan
    }
}

fun GeocodeResponseDto.formatLocationName(): String? {
    val localityName = locality.withoutAdministrativePrefix()
    val cityName = city.withoutAdministrativePrefix()
    return if (localityName.isNotBlank() && cityName.isNotBlank()) "$localityName, $cityName" else null
}
