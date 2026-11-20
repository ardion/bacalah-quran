package id.ardion.quran.data.repository

import id.ardion.quran.domain.irepository.LastReadRepository
import id.ardion.quran.domain.model.LastReadInfo
import id.ardion.quran.domain.model.MultiLastReadInfo
import id.ardion.quran.domain.model.ReadMode
import id.ardion.quran.utils.KVStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LastReadRepositoryImpl : LastReadRepository {
    private val json = Json { ignoreUnknownKeys = true }

    private val keySurah = "key_surah_last_read"
    private val keyMushaf = "key_mushaf_last_read"

    private fun loadSurahFromStorage(): LastReadInfo? {
        val str = KVStorage.getString(keySurah) ?: return null
        return try {
            json.decodeFromString<LastReadInfo>(str)
        } catch (_: Exception) {
            null
        }
    }

    private fun loadMushafFromStorage(): LastReadInfo? {
        val str = KVStorage.getString(keyMushaf) ?: return null
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
    override val multiLastRead: StateFlow<MultiLastReadInfo> = _multiLastRead

    override fun saveLastReadSurah(surahNumber: Int, surahName: String, ayahNumber: Int) {
        val info = LastReadInfo(
            readMode = ReadMode.SURAH,
            surahNumber = surahNumber,
            surahName = surahName,
            ayahNumber = ayahNumber
        )
        try {
            KVStorage.putString(keySurah, json.encodeToString(info))
        } catch (_: Exception) {}
        _multiLastRead.value = _multiLastRead.value.copy(surahLastRead = info)
    }

    override fun saveLastReadPage(pageNumber: Int, surahName: String, ayahNumber: Int) {
        val info = LastReadInfo(
            readMode = ReadMode.MUSHAF,
            pageNumber = pageNumber,
            surahName = if (surahName.isNotEmpty()) surahName else "Halaman $pageNumber",
            ayahNumber = ayahNumber
        )
        try {
            KVStorage.putString(keyMushaf, json.encodeToString(info))
        } catch (_: Exception) {}
        _multiLastRead.value = _multiLastRead.value.copy(mushafLastRead = info)
    }

    override fun clearSurahLastRead() {
        KVStorage.remove(keySurah)
        _multiLastRead.value = _multiLastRead.value.copy(surahLastRead = null)
    }

    override fun clearMushafLastRead() {
        KVStorage.remove(keyMushaf)
        _multiLastRead.value = _multiLastRead.value.copy(mushafLastRead = null)
    }
}
