package id.ardion.quran.domain.model

import kotlinx.datetime.LocalDate

enum class PrayerTimeSource {
    KEMENAG,
    CALCULATED
}

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val date: LocalDate,
    val latitude: Double,
    val longitude: Double,
    val qiblaBearing: Double,
    val locationName: String?,
    val isLiveLocation: Boolean,
    val source: PrayerTimeSource
)
