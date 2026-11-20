package id.ardion.quran.data.mapper

import id.ardion.quran.data.response.AladhanDataDto
import id.ardion.quran.data.response.GeocodeResponseDto
import id.ardion.quran.data.response.NominatimAddressDto
import id.ardion.quran.domain.model.PrayerTimes

fun AladhanDataDto.toDomain(
    latitude: Double,
    longitude: Double,
    qiblaBearing: Double,
    locationName: String,
    isLiveLocation: Boolean = false
): PrayerTimes {
    return PrayerTimes(
        fajr = timings.fajr,
        sunrise = timings.sunrise,
        dhuhr = timings.dhuhr,
        asr = timings.asr,
        maghrib = timings.maghrib,
        isha = timings.isha,
        dateReadable = date?.readable ?: "-",
        hijriDate = "${date?.hijri?.date ?: ""} ${date?.hijri?.month?.en ?: ""}".trim(),
        latitude = latitude,
        longitude = longitude,
        qiblaBearing = qiblaBearing,
        locationName = locationName,
        isLiveLocation = isLiveLocation
    )
}

fun NominatimAddressDto.formatLocationName(): String {
    val kelurahan = (suburb ?: village ?: neighbourhood ?: "")
        .replace("Kelurahan ", "")
        .replace("Kecamatan ", "")
        .trim()
    val kota = (city ?: town ?: city_district ?: "")
        .replace("Kota ", "")
        .replace("Kabupaten ", "")
        .trim()

    return if (kelurahan.isNotBlank() && kota.isNotBlank() && kelurahan != kota) {
        "$kelurahan, $kota"
    } else if (kelurahan.isNotBlank()) {
        kelurahan
    } else {
        ""
    }
}

fun GeocodeResponseDto.formatLocationName(): String {
    val localityStr = (locality ?: "").replace("Kecamatan ", "").trim()
    val cityStr = (city ?: "").replace("Kota ", "").replace("Kabupaten ", "").trim()

    return if (localityStr.isNotBlank() && cityStr.isNotBlank()) {
        "$localityStr, $cityStr"
    } else {
        ""
    }
}
