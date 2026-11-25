package id.ardion.quran.fake

import id.ardion.quran.audio.AudioPlayback
import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.domain.model.LastRead
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.domain.model.SurahLastRead
import id.ardion.quran.domain.repository.LastReadRepository
import id.ardion.quran.domain.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakeResponseCache(initial: Map<String, String> = emptyMap()) : ResponseCache {
    val entries = initial.toMutableMap()
    override fun read(key: String): String? = entries[key]
    override fun write(key: String, value: String) { entries[key] = value }
    override fun remove(key: String) { entries.remove(key) }
}

class FakeAudioPlayback : AudioPlayback {
    override val isAvailable = true
    override fun play(url: String, onCompletion: (() -> Unit)?) = Unit
    override fun pause() = Unit
    override fun stop() = Unit
}

class FakeLastReadRepository : LastReadRepository {
    private val state = MutableStateFlow(LastRead())
    override val lastRead: StateFlow<LastRead> = state
    override fun saveSurah(position: SurahLastRead) = state.update { it.copy(surah = position) }
    override fun saveMushaf(position: MushafLastRead) = state.update { it.copy(mushaf = position) }
    override fun clearSurah() = state.update { it.copy(surah = null) }
    override fun clearMushaf() = state.update { it.copy(mushaf = null) }
}

class FakeQuranRepository : QuranRepository {
    var surahs: Result<List<SurahItem>> = Result.success(emptyList())
    var surahDetail: (Int) -> Result<SurahDetail> = { Result.failure(IllegalStateException("not set")) }
    var pageDetail: (Int) -> Result<PageDetail> = { Result.success(PageDetail(it, emptyList())) }
    val surahRequests = mutableListOf<Int>()
    val pageRequests = mutableListOf<Int>()

    override suspend fun getAllSurahs() = surahs
    override suspend fun getSurahDetail(surahNumber: Int): Result<SurahDetail> {
        surahRequests += surahNumber
        return surahDetail(surahNumber)
    }
    override suspend fun getPageDetail(pageNumber: Int): Result<PageDetail> {
        pageRequests += pageNumber
        return pageDetail(pageNumber)
    }
}
