package id.ardion.quran.domain.model

import androidx.compose.ui.text.AnnotatedString
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

data class SurahDetail(
    val number: Int,
    val name: String,
    val englishName: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val ayahs: List<AyahDetail>
)

data class PageDetail(
    val pageNumber: Int,
    val ayahs: List<AyahDetail>
)

data class Ayah(
    val numberInSurah: Int,
    val textTajweed: AnnotatedString,
    val audioUrl: String?,
    val juz: Int,
    val page: Int
)

data class SurahItem(
    val number: Int,
    val name: String,
    val englishName: String,
    val translation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

data class AyahDetail(
    val numberInSurah: Int,
    val textTajweed: AnnotatedString,
    val translation: String,
    val transliteration: String?,
    val audioUrl: String?,
    val surahName: String? = null,
    val revelationType: String? = null,
    val juz: Int? = null
)
