package id.ardion.quran.tajweed

import kotlin.test.Test
import kotlin.test.assertEquals

class TajweedTextTest {

    @Test
    fun stripsMarkupAndRecordsSpan() {
        val result = parseTajweedText("ab[g[نّ]cd")
        assertEquals("abنّcd", result.text)
        assertEquals(listOf(TajweedSpan(2, 4, "g")), result.spans)
    }

    @Test
    fun acceptsNumberedTags() {
        val result = parseTajweedText("[h:9421[ٱ]ل")
        assertEquals("ٱل", result.text)
        assertEquals(listOf(TajweedSpan(0, 1, "h")), result.spans)
    }

    @Test
    fun unclosedTagRunsToEnd() {
        val result = parseTajweedText("x[q[yz")
        assertEquals("xyz", result.text)
        assertEquals(listOf(TajweedSpan(1, 3, "q")), result.spans)
    }

    @Test
    fun emptySpansAreDropped() {
        assertEquals(emptyList(), parseTajweedText("a[n[]b").spans)
    }

    @Test
    fun normalisesInvisibleAndSpaceCharacters() {
        val result = parseTajweedText("a\u200Bb\u00A0c\u0672")
        assertEquals("ab c\u0670", result.text)
    }

    @Test
    fun emptyInputGivesEmptyText() {
        val result = parseTajweedText("")
        assertEquals("", result.text)
        assertEquals(emptyList(), result.spans)
    }

    @Test
    fun strayClosingBracketIsIgnored() {
        assertEquals("ab", parseTajweedText("a]b").text)
    }

    @Test
    fun nestedTagsProduceBothSpans() {
        val result = parseTajweedText("[g[a[q[b]]")
        assertEquals("ab", result.text)
        assertEquals(listOf(TajweedSpan(1, 2, "q"), TajweedSpan(0, 2, "g")), result.spans)
    }
}
