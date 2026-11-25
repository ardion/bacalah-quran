package id.ardion.quran.domain.model

data class SurahLastRead(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int
)

data class MushafLastRead(
    val pageNumber: Int,
    val surahName: String? = null,
    val ayahNumber: Int? = null
)

data class LastRead(
    val surah: SurahLastRead? = null,
    val mushaf: MushafLastRead? = null
)
