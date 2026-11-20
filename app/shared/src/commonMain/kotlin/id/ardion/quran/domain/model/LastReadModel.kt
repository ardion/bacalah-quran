package id.ardion.quran.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReadMode {
    SURAH,
    MUSHAF
}

@Serializable
data class LastReadInfo(
    val readMode: ReadMode = ReadMode.SURAH,
    val surahNumber: Int = -1,
    val surahName: String = "Al-Fatihah",
    val pageNumber: Int = 1,
    val ayahNumber: Int = -1
)

data class MultiLastReadInfo(
    val surahLastRead: LastReadInfo? = null,
    val mushafLastRead: LastReadInfo? = null
)
