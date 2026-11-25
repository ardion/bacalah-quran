package id.ardion.quran.config

object ApiConfig {
    const val QURAN_BASE_URL = "https://api.alquran.cloud/v1/"

    const val SURAH_DETAIL_EDITIONS = "quran-tajweed,id.indonesian,en.transliteration,ar.alafasy"
    const val TAJWEED_EDITION = "quran-tajweed"

    const val AYAH_AUDIO_URL_TEMPLATE = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/{n}.mp3"

    const val PRAYER_TIMES_URL = "https://api.aladhan.com/v1/timings"
    const val PRAYER_CALENDAR_URL = "https://api.aladhan.com/v1/calendar"

    const val PRAYER_CALCULATION_METHOD = 20

    const val KEMENAG_IHTIYATH_TUNE = "3,3,-3,4,2,3,0,3,0"

    const val KEMENAG_SCHEDULE_URL = "https://api.myquran.com/v2/sholat"

    const val NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse"
    const val NOMINATIM_USER_AGENT = "BacalahQuranApp/1.0"
    const val BIGDATACLOUD_REVERSE_URL = "https://api.bigdatacloud.net/data/reverse-geocode-client"

    fun ayahAudioUrl(globalAyahNumber: Int): String =
        AYAH_AUDIO_URL_TEMPLATE.replace("{n}", globalAyahNumber.toString())
}
