package id.ardion.quran.data.mapper

import id.ardion.quran.config.ApiConfig
import id.ardion.quran.data.response.AyahDto
import id.ardion.quran.data.response.MultiEditionSurahDto
import id.ardion.quran.data.response.PageDataDto
import id.ardion.quran.data.response.SurahListItemDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class QuranMapperTest {

    private fun edition(vararg ayahs: AyahDto) =
        MultiEditionSurahDto(number = 1, englishName = "Al-Faatiha", ayahs = ayahs.toList())

    @Test
    fun emptyEditionsMapToNull() {
        assertNull(mapToSurahDetail(emptyList()))
        assertNull(mapToSurahDetail(listOf(MultiEditionSurahDto(number = 1))))
    }

    @Test
    fun mergesTranslationTransliterationAndAudioByPosition() {
        val detail = mapToSurahDetail(
            listOf(
                edition(AyahDto(number = 1, numberInSurah = 1, text = "[g[ب]")),
                edition(AyahDto(number = 1, numberInSurah = 1, text = "Dengan nama Allah")),
                edition(AyahDto(number = 1, numberInSurah = 1, text = "Bismillah")),
                edition(AyahDto(number = 1, numberInSurah = 1, audio = "https://audio/1.mp3"))
            )
        )

        val ayah = assertNotNull(detail).ayahs.single()
        assertEquals("ب", ayah.textTajweed.text)
        assertEquals("Dengan nama Allah", ayah.translation)
        assertEquals("Bismillah", ayah.transliteration)
        assertEquals("https://audio/1.mp3", ayah.audioUrl)
    }

    @Test
    fun missingTranslationEditionsDoNotCrash() {
        val detail = mapToSurahDetail(listOf(edition(AyahDto(number = 7, numberInSurah = 7, text = "[n[ا]"))))
        val ayah = assertNotNull(detail).ayahs.single()
        assertEquals("", ayah.translation)
        assertNull(ayah.transliteration)
        assertEquals(ApiConfig.ayahAudioUrl(7), ayah.audioUrl)
    }

    @Test
    fun ayahsWithoutNumbersAreDropped() {
        val page = mapToPageDetail(
            PageDataDto(ayahs = listOf(AyahDto(number = null, numberInSurah = 1), AyahDto(number = 2, numberInSurah = 2))),
            pageNumber = 1
        )
        assertEquals(listOf(2), page.ayahs.map { it.numberInSurah })
    }

    @Test
    fun surahListSkipsItemsWithoutNumberAndDefaultsText() {
        val surahs = listOf(SurahListItemDto(number = null), SurahListItemDto(number = 2)).toDomain()
        assertEquals(1, surahs.size)
        assertEquals("", surahs.single().englishName)
    }
}
