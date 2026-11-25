package id.ardion.quran.data.mapper

import id.ardion.quran.data.response.AladhanDataDto
import id.ardion.quran.data.response.GeocodeResponseDto
import id.ardion.quran.data.response.KemenagDayDto
import id.ardion.quran.data.response.NominatimAddressDto
import id.ardion.quran.data.response.TimingsDto
import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.PrayerTimeSource
import id.ardion.quran.domain.model.PrayerTimes
import kotlinx.datetime.LocalDate

const val MISSING_TIME = "--:--"

data class PrayerContext(
    val location: LocationCoordinates,
    val qiblaBearing: Double,
    val locationName: String?,
    val isLiveLocation: Boolean,
    val date: LocalDate
)

fun AladhanDataDto.toDomain(context: PrayerContext): PrayerTimes? = timings?.toDomain(context)

fun TimingsDto.toDomain(context: PrayerContext): PrayerTimes = context.toPrayerTimes(
    fajr = fajr.normalizedTime(),
    sunrise = sunrise.normalizedTime(),
    dhuhr = dhuhr.normalizedTime(),
    asr = asr.normalizedTime(),
    maghrib = maghrib.normalizedTime(),
    isha = isha.normalizedTime(),
    source = PrayerTimeSource.CALCULATED
)

private fun String?.normalizedTime(): String =
    this?.substringBefore(" ")?.trim()?.takeIf { it.isNotEmpty() } ?: MISSING_TIME

fun KemenagDayDto.toDomain(context: PrayerContext): PrayerTimes? =
    context.toPrayerTimes(
        fajr = fajr ?: return null,
        sunrise = sunrise ?: return null,
        dhuhr = dhuhr ?: return null,
        asr = asr ?: return null,
        maghrib = maghrib ?: return null,
        isha = isha ?: return null,
        source = PrayerTimeSource.KEMENAG
    )

private fun PrayerContext.toPrayerTimes(
    fajr: String,
    sunrise: String,
    dhuhr: String,
    asr: String,
    maghrib: String,
    isha: String,
    source: PrayerTimeSource
) = PrayerTimes(
    fajr = fajr,
    sunrise = sunrise,
    dhuhr = dhuhr,
    asr = asr,
    maghrib = maghrib,
    isha = isha,
    date = date,
    latitude = location.latitude,
    longitude = location.longitude,
    qiblaBearing = qiblaBearing,
    locationName = locationName,
    isLiveLocation = isLiveLocation,
    source = source
)

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
