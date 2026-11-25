package id.ardion.quran.presentation.mushaf

import kotlin.test.Test
import kotlin.test.assertEquals

class MushafNavigationTest {

    @Test
    fun surahStartPagesMatchMadaniMushaf() {
        assertEquals(1, getSurahStartPage(1))
        assertEquals(312, getSurahStartPage(20))
        assertEquals(440, getSurahStartPage(36))
        assertEquals(604, getSurahStartPage(SURAH_COUNT))
    }

    @Test
    fun juzStartPagesMatchMadaniMushaf() {
        assertEquals(1, getJuzStartPage(1))
        assertEquals(121, getJuzStartPage(7))
        assertEquals(201, getJuzStartPage(11))
        assertEquals(582, getJuzStartPage(JUZ_COUNT))
    }

    @Test
    fun outOfRangeInputIsClamped() {
        assertEquals(1, getSurahStartPage(-5))
        assertEquals(604, getSurahStartPage(500))
        assertEquals(1, getJuzStartPage(0))
        assertEquals(582, getJuzStartPage(99))
    }

    @Test
    fun arabicDigits() = assertEquals("١٢٠", toArabicNumbers(120))
}
