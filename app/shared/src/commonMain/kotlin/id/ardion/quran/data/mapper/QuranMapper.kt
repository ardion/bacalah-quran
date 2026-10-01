package id.ardion.quran.data.mapper

import id.ardion.quran.data.response.MultiEditionSurahDto
import id.ardion.quran.data.response.PageDataDto
import id.ardion.quran.data.response.SurahListItemDto
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.utils.parseTajweedText

fun SurahListItemDto.toDomain(): SurahItem {
    return SurahItem(
        number = number,
        name = name,
        englishName = englishName,
        translation = englishNameTranslation,
        numberOfAyahs = numberOfAyahs,
        revelationType = revelationType
    )
}

fun List<SurahListItemDto>.toDomain(): List<SurahItem> {
    return map { it.toDomain() }
}

fun mapToSurahDetail(editions: List<MultiEditionSurahDto>): SurahDetail {
    val tajweedEdition = editions.firstOrNull { it.ayahs.firstOrNull()?.text?.contains("[") == true } ?: editions.first()
    val translationEdition = editions.getOrNull(1) ?: tajweedEdition
    val transliterationEdition = editions.getOrNull(2)
    val audioEdition = editions.getOrNull(3) ?: tajweedEdition

    val ayahs = tajweedEdition.ayahs.mapIndexed { index, tajweedAyah ->
        AyahDetail(
            numberInSurah = tajweedAyah.numberInSurah,
            textTajweed = parseTajweedText(tajweedAyah.text),
            translation = translationEdition.ayahs.getOrNull(index)?.text ?: "",
            transliteration = transliterationEdition?.ayahs?.getOrNull(index)?.text,
            audioUrl = audioEdition.ayahs.getOrNull(index)?.audio ?: "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${tajweedAyah.number}.mp3"
        )
    }

    return SurahDetail(
        number = tajweedEdition.number,
        name = tajweedEdition.name,
        englishName = tajweedEdition.englishName,
        revelationType = tajweedEdition.revelationType,
        numberOfAyahs = tajweedEdition.numberOfAyahs,
        ayahs = ayahs
    )
}

fun mapToPageDetail(data: PageDataDto, pageNumber: Int): PageDetail {
    val ayahs = data.ayahs.map { ayah ->
        AyahDetail(
            numberInSurah = ayah.numberInSurah,
            textTajweed = parseTajweedText(ayah.text),
            translation = "",
            transliteration = null,
            audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${ayah.number}.mp3",
            surahName = ayah.surah?.englishName ?: ayah.surah?.name,
            revelationType = ayah.surah?.revelationType,
            juz = ayah.juz
        )
    }

    return PageDetail(
        pageNumber = pageNumber,
        ayahs = ayahs
    )
}
