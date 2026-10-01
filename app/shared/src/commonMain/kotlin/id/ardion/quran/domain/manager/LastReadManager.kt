package id.ardion.quran.domain.manager

import id.ardion.quran.domain.model.LastReadInfo
import id.ardion.quran.domain.model.ReadMode
import id.ardion.quran.utils.KVStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class MultiLastReadInfo(
    val surahLastRead: LastReadInfo? = null,
    val mushafLastRead: LastReadInfo? = null
)

object LastReadManager {
    private val json = Json { ignoreUnknownKeys = true }

    private const val KEY_SURAH = "key_surah_last_read"
    private const val KEY_MUSHAF = "key_mushaf_last_read"

    private fun loadSurahFromStorage(): LastReadInfo? {
        val str = KVStorage.getString(KEY_SURAH) ?: return null
        return try {
            json.decodeFromString<LastReadInfo>(str)
        } catch (_: Exception) {
            null
        }
    }

    private fun loadMushafFromStorage(): LastReadInfo? {
        val str = KVStorage.getString(KEY_MUSHAF) ?: return null
        return try {
            json.decodeFromString<LastReadInfo>(str)
        } catch (_: Exception) {
            null
        }
    }

    private val _multiLastRead = MutableStateFlow(
        MultiLastReadInfo(
            surahLastRead = loadSurahFromStorage(),
            mushafLastRead = loadMushafFromStorage()
        )
    )
    val multiLastRead: StateFlow<MultiLastReadInfo> = _multiLastRead

    fun saveLastReadSurah(surahNumber: Int, surahName: String, ayahNumber: Int) {
        val info = LastReadInfo(
            readMode = ReadMode.SURAH,
            surahNumber = surahNumber,
            surahName = surahName,
            ayahNumber = ayahNumber
        )
        try {
            KVStorage.putString(KEY_SURAH, json.encodeToString(info))
        } catch (_: Exception) {}
        _multiLastRead.value = _multiLastRead.value.copy(surahLastRead = info)
    }

    fun saveLastReadPage(pageNumber: Int, surahName: String = "", ayahNumber: Int = 1) {
        val info = LastReadInfo(
            readMode = ReadMode.MUSHAF,
            pageNumber = pageNumber,
            surahName = if (surahName.isNotEmpty()) surahName else "Halaman $pageNumber",
            ayahNumber = ayahNumber
        )
        try {
            KVStorage.putString(KEY_MUSHAF, json.encodeToString(info))
        } catch (_: Exception) {}
        _multiLastRead.value = _multiLastRead.value.copy(mushafLastRead = info)
    }

    fun clearSurahLastRead() {
        KVStorage.remove(KEY_SURAH)
        _multiLastRead.value = _multiLastRead.value.copy(surahLastRead = null)
    }

    fun clearMushafLastRead() {
        KVStorage.remove(KEY_MUSHAF)
        _multiLastRead.value = _multiLastRead.value.copy(mushafLastRead = null)
    }
}
