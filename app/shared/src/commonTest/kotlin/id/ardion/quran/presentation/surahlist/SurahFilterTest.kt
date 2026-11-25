package id.ardion.quran.presentation.surahlist

import id.ardion.quran.domain.model.SurahItem
import kotlin.test.Test
import kotlin.test.assertEquals

class SurahFilterTest {
    private val surahs = listOf(
        SurahItem(1, "الفاتحة", "Al-Faatiha", "The Opening", 7, "Meccan"),
        SurahItem(36, "يس", "Yaseen", "Yaseen", 83, "Meccan")
    )

    @Test
    fun blankQueryKeepsEverything() = assertEquals(surahs, surahs.filterBy("  "))

    @Test
    fun matchesNameCaseInsensitively() = assertEquals(listOf(36), surahs.filterBy("yas").map { it.number })

    @Test
    fun matchesNumber() = assertEquals(listOf(36), surahs.filterBy("36").map { it.number })

    @Test
    fun noMatchGivesEmptyList() = assertEquals(emptyList(), surahs.filterBy("xyz"))
}
