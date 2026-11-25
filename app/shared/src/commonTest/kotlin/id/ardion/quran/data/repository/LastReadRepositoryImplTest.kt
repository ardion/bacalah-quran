package id.ardion.quran.data.repository

import id.ardion.quran.data.local.KeyValueStore
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.SurahLastRead
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeKeyValueStore(initial: Map<String, String> = emptyMap()) : KeyValueStore {
    val values = initial.toMutableMap()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) { values[key] = value }
    override fun remove(key: String) { values.remove(key) }
}

class LastReadRepositoryImplTest {

    @Test
    fun savedPositionsSurviveRestart() {
        val store = FakeKeyValueStore()
        LastReadRepositoryImpl(store).apply {
            saveSurah(SurahLastRead(36, "Yasin", 15))
            saveMushaf(MushafLastRead(50, "Al-Baqarah", 10))
        }

        val restored = LastReadRepositoryImpl(store).lastRead.value
        assertEquals(SurahLastRead(36, "Yasin", 15), restored.surah)
        assertEquals(MushafLastRead(50, "Al-Baqarah", 10), restored.mushaf)
    }

    @Test
    fun clearRemovesOnlyThatPosition() {
        val repository = LastReadRepositoryImpl(FakeKeyValueStore())
        repository.saveSurah(SurahLastRead(1, "Al-Fatihah", 1))
        repository.saveMushaf(MushafLastRead(2))
        repository.clearSurah()

        assertNull(repository.lastRead.value.surah)
        assertEquals(MushafLastRead(2), repository.lastRead.value.mushaf)
    }

    @Test
    fun corruptJsonIsDiscarded() {
        val store = FakeKeyValueStore(mapOf(KEY_SURAH_LAST_READ to "{not json"))
        assertNull(LastReadRepositoryImpl(store).lastRead.value.surah)
        assertNull(store.values[KEY_SURAH_LAST_READ])
    }

    @Test
    fun legacySentinelValuesBecomeNull() {
        val store = FakeKeyValueStore(
            mapOf(
                KEY_SURAH_LAST_READ to """{"readMode":"SURAH","surahNumber":-1,"surahName":"Al-Fatihah","pageNumber":1,"ayahNumber":-1}""",
                KEY_MUSHAF_LAST_READ to """{"readMode":"MUSHAF","surahNumber":-1,"surahName":"","pageNumber":12,"ayahNumber":-1}"""
            )
        )
        val lastRead = LastReadRepositoryImpl(store).lastRead.value
        assertNull(lastRead.surah)
        assertEquals(MushafLastRead(pageNumber = 12, surahName = null, ayahNumber = null), lastRead.mushaf)
    }

    @Test
    fun outOfRangePageIsIgnored() {
        val store = FakeKeyValueStore(mapOf(KEY_MUSHAF_LAST_READ to """{"pageNumber":999}"""))
        assertNull(LastReadRepositoryImpl(store).lastRead.value.mushaf)
    }
}
