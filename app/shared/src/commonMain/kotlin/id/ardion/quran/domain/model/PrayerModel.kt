package id.ardion.quran.domain.model

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val dateReadable: String,
    val hijriDate: String,
    val latitude: Double,
    val longitude: Double,
    val qiblaBearing: Double,
    val locationName: String?,
    val isLiveLocation: Boolean
)
