package id.ardion.quran.domain.model

import androidx.compose.ui.text.AnnotatedString

data class SurahDetail(
    val number: Int,
    val name: String,
    val englishName: String,
    val revelationType: String,
    val numberOfAyahs: Int,
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
    val audioUrl: String?
)
