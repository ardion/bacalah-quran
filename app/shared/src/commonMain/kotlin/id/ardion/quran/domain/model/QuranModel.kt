package id.ardion.quran.domain.model

import id.ardion.quran.tajweed.TajweedText

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
    val textTajweed: TajweedText,
    val translation: String,
    val transliteration: String?,
    val audioUrl: String?,
    val surahName: String? = null,
    val revelationType: String? = null,
    val juz: Int? = null
)
