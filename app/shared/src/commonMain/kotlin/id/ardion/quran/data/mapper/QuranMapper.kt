package id.ardion.quran.data.mapper

import id.ardion.quran.config.ApiConfig
import id.ardion.quran.data.response.AyahDto
import id.ardion.quran.data.response.MultiEditionSurahDto
import id.ardion.quran.data.response.PageDataDto
import id.ardion.quran.data.response.SurahListItemDto
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.tajweed.parseTajweedText

fun SurahListItemDto.toDomain(): SurahItem? {
    val surahNumber = number ?: return null
    return SurahItem(
        number = surahNumber,
        name = name.orEmpty(),
        englishName = englishName.orEmpty(),
        translation = englishNameTranslation.orEmpty(),
        numberOfAyahs = numberOfAyahs ?: 0,
        revelationType = revelationType.orEmpty()
    )
}

fun List<SurahListItemDto>.toDomain(): List<SurahItem> = mapNotNull { it.toDomain() }

fun mapToSurahDetail(editions: List<MultiEditionSurahDto>): SurahDetail? {
    val tajweedEdition = editions.firstOrNull { it.ayahs.firstOrNull()?.text?.contains("[") == true }
        ?: editions.firstOrNull { it.ayahs.isNotEmpty() }
        ?: return null
    val translationEdition = editions.getOrNull(1)
    val transliterationEdition = editions.getOrNull(2)
    val audioEdition = editions.getOrNull(3)

    val ayahs = tajweedEdition.ayahs.mapIndexedNotNull { index, ayah ->
        ayah.toAyahDetail(
            translation = translationEdition?.ayahs?.getOrNull(index)?.text.orEmpty(),
            transliteration = transliterationEdition?.ayahs?.getOrNull(index)?.text,
            audioUrl = audioEdition?.ayahs?.getOrNull(index)?.audio
        )
    }

    return SurahDetail(
        number = tajweedEdition.number ?: return null,
        name = tajweedEdition.name.orEmpty(),
        englishName = tajweedEdition.englishName.orEmpty(),
        revelationType = tajweedEdition.revelationType.orEmpty(),
        numberOfAyahs = tajweedEdition.numberOfAyahs ?: ayahs.size,
        ayahs = ayahs
    )
}

fun mapToPageDetail(data: PageDataDto, pageNumber: Int): PageDetail =
    PageDetail(
        pageNumber = pageNumber,
        ayahs = data.ayahs.mapNotNull { it.toAyahDetail() }
    )

private fun AyahDto.toAyahDetail(
    translation: String = "",
    transliteration: String? = null,
    audioUrl: String? = null
): AyahDetail? {
    val globalNumber = number ?: return null
    val numberInSurah = numberInSurah ?: return null
    return AyahDetail(
        numberInSurah = numberInSurah,
        textTajweed = parseTajweedText(text.orEmpty()),
        translation = translation,
        transliteration = transliteration,
        audioUrl = audioUrl ?: audio ?: ApiConfig.ayahAudioUrl(globalNumber),
        surahName = surah?.englishName ?: surah?.name,
        revelationType = surah?.revelationType,
        juz = juz
    )
}
